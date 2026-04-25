FROM quay.io/keycloak/keycloak:latest
COPY realm-export.json /opt/keycloak/data/import/realm-export.json
CMD ["start", "--import-realm"]
