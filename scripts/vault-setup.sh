#!/bin/bash
# Vault setup script for local development
# Runs after Vault dev container starts

echo "=== TaxFlow Vault Setup ==="

sleep 3

VAULT_TOKEN=${VAULT_ROOT_TOKEN:-dev_root_token}

echo "Vault is ready. Root token: $VAULT_TOKEN"
echo "KV secrets engine is available at: secret/"

echo "Certificate storage path: secret/data/taxflow/certs/{tenantId}/{certId}"
echo ""
echo "=== Vault setup complete ==="
