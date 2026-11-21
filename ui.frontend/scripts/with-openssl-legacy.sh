#!/bin/sh
# webpack 4 (react-scripts 4) hashes with OpenSSL MD4. Node 17+ needs the
# legacy provider; Node 12/16, used by the frontend-maven-plugin, do not.
major=$(node -p "process.versions.node.split('.')[0]")
if [ "$major" -ge 17 ]; then
  case "${NODE_OPTIONS:-}" in
    *--openssl-legacy-provider*) ;;
    "") NODE_OPTIONS="--openssl-legacy-provider" ;;
    *) NODE_OPTIONS="${NODE_OPTIONS} --openssl-legacy-provider" ;;
  esac
  export NODE_OPTIONS
fi
exec "$@"
