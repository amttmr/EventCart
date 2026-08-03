export const appConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  authEnabled: (import.meta.env.VITE_AUTH_ENABLED ?? 'true') !== 'false',
  defaultCustomerId: import.meta.env.VITE_DEFAULT_CUSTOMER_ID ?? 'customer-1',
  keycloak: {
    url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8088',
    realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'eventcart',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'eventcart-gateway',
  },
}
