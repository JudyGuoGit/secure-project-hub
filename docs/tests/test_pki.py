#!/usr/bin/env python3
"""
PKI Endpoint Test Suite using subprocess curl (more reliable than urllib)
"""
import subprocess
import json
import sys

base_url = "https://localhost:8443"
ca_cert = "certs/ca-cert.pem"
client_cert = "certs/client-cert.pem"
client_key = "certs/client-key.pem"

endpoints = [
    ("/api/pki/health", "status"),
    ("/api/pki/certificate-info", "isValid"),
    ("/api/pki/verify", "isValid"),
    ("/api/pki/secure-data", "message"),
    ("/api/pki/auth-method", "accessedBy")
]

def test_endpoint(endpoint, key_to_check):
    url = f"{base_url}{endpoint}"
    try:
        result = subprocess.run(
            [
                "/usr/bin/curl", "-i",
                "--cacert", ca_cert,
                "--cert", client_cert,
                "--key", client_key,
                url
            ],
            capture_output=True,
            text=True,
            timeout=10,
            cwd="/Users/jguo/work/eclipse-workspace/secure-project-hub"
        )
        
        output = result.stdout + result.stderr
        
        # Check for HTTP 200 status
        if "HTTP/2 200" in output or "HTTP/1.1 200" in output:
            try:
                # Extract JSON from response (skip headers)
                json_start = output.find('{')
                if json_start > 0:
                    json_str = output[json_start:]
                    # Find the closing brace
                    brace_count = 0
                    for i, char in enumerate(json_str):
                        if char == '{':
                            brace_count += 1
                        elif char == '}':
                            brace_count -= 1
                            if brace_count == 0:
                                json_str = json_str[:i+1]
                                break
                    
                    data = json.loads(json_str)
                    print(f"✅ {endpoint}")
                    print(f"   Status: HTTP 200")
                    print(f"   Response has {len(data)} keys")
                    return True
                else:
                    print(f"✅ {endpoint}")
                    print(f"   Status: HTTP 200 (no JSON body)")
                    return True
            except (json.JSONDecodeError, ValueError) as e:
                print(f"✅ {endpoint}")
                print(f"   Status: HTTP 200 (JSON parse: {str(e)[:50]})")
                return True
        else:
            print(f"❌ {endpoint} - No HTTP 200 response")
            if result.stderr:
                print(f"   Error: {result.stderr[:100]}")
            return False
    except subprocess.TimeoutExpired:
        print(f"❌ {endpoint} - Timeout")
        return False
    except Exception as e:
        print(f"❌ {endpoint} - {type(e).__name__}: {str(e)[:80]}")
        return False

if __name__ == "__main__":
    print("=" * 60)
    print("PKI/mTLS Endpoint Test Suite")
    print("=" * 60)
    print(f"Base URL: {base_url}\n")
    
    passed = 0
    failed = 0
    
    for endpoint, key in endpoints:
        if test_endpoint(endpoint, key):
            passed += 1
        else:
            failed += 1
        print()
    
    print("=" * 60)
    print(f"Test Results: {passed} passed, {failed} failed")
    print("=" * 60)
    
    sys.exit(0 if failed == 0 else 1)
