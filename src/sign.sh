openssl dgst -sha1 -binary < "$1" | openssl dgst -dss1 -sign ~/.dsakeys/dsa_priv.pem | openssl enc -base64
