export const authConfig = {
  clientId: 'client-flow',
  authorizationEndpoint: 'http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/auth',
  tokenEndpoint: 'http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/token',
  redirectUri: 'http://localhost:5173',
  scope: 'openid profile email offline_access',
  onRefreshTokenExpire: (event) => event.logIn(),

  // Add these properties to prevent auto-login
  autoLogin: false,  // This is the key setting!
  
  // Add logout endpoint
  logoutEndpoint: 'http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/logout',
  
  // Optional: where to redirect after logout
  logoutRedirect: 'http://localhost:5173',
}