import { useContext, useState, useEffect } from "react"
import { AuthContext } from "react-oauth2-code-pkce"
import { LogIn, LogOut, Shield, Mail, Key, Copy, Check, AlertCircle } from "lucide-react"

function App() {
  const { token, tokenData, logIn, logOut, idToken, idTokenData, error } = useContext(AuthContext);
  const [copied, setCopied] = useState(false);
  const [showToken, setShowToken] = useState(false);
  const [loginError, setLoginError] = useState(null);

  const isAuthenticated = !!token;

  const userInfo = idTokenData || tokenData;
  const username = userInfo?.preferred_username || userInfo?.name || "User";
  const email = userInfo?.email;

  // Debug logging
  useEffect(() => {
    console.log('Auth State:', { 
      isAuthenticated, 
      hasToken: !!token, 
      hasLogInFunction: !!logIn,
      error 
    });
  }, [isAuthenticated, token, logIn, error]);

  // Handle auth errors
  useEffect(() => {
    if (error) {
      setLoginError(error);
      console.error('Auth Error:', error);
    }
  }, [error]);

  const copyToken = () => {
    navigator.clipboard.writeText(token);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const getTimeRemaining = () => {
    if (!tokenData?.exp) return null;
    const now = Math.floor(Date.now() / 1000);
    const seconds = tokenData.exp - now;
    const minutes = Math.floor(seconds / 60);
    return seconds > 0 ? `${minutes}m ${seconds % 60}s` : "Expired";
  };

  const handleLogin = async () => {
    console.log('Login button clicked');
    setLoginError(null);
    
    try {
      if (!logIn) {
        throw new Error('logIn function not available from AuthContext');
      }
      
      console.log('Calling logIn()...');
      await logIn();
      console.log('logIn() called successfully');
    } catch (err) {
      console.error('Login error:', err);
      setLoginError(err.message || 'Failed to initiate login');
    }
  };

  const handleLogout = () => {
    console.log('Logout button clicked');
    try {
      // Clear local token storage first
      logOut();
      
      // Redirect to Keycloak logout
      const logoutUrl = new URL('http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/logout');
      logoutUrl.searchParams.append('post_logout_redirect_uri', 'http://localhost:5173');
      
      if (idToken) {
        logoutUrl.searchParams.append('id_token_hint', idToken);
      }
      
      console.log('Redirecting to:', logoutUrl.toString());
      window.location.href = logoutUrl.toString();
    } catch (err) {
      console.error('Logout error:', err);
      setLoginError(err.message || 'Failed to logout');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="w-full max-w-2xl">
        
        {/* Header */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-3 mb-2">
            <Shield className="w-10 h-10 text-blue-600" />
            <h1 className="text-4xl font-bold text-gray-800">Keycloak Auth</h1>
          </div>
          <p className="text-gray-600">OAuth2 with PKCE Flow</p>
        </div>

        {/* Error Alert */}
        {loginError && (
          <div className="mb-4 bg-red-50 border border-red-200 rounded-lg p-4 flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
            <div>
              <h3 className="font-semibold text-red-900">Error</h3>
              <p className="text-sm text-red-700">{loginError}</p>
            </div>
          </div>
        )}

        {/* Main Card */}
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
          
          {/* Not Authenticated */}
          {!isAuthenticated && (
            <div className="p-12 text-center">
              <div className="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-6">
                <LogIn className="w-10 h-10 text-blue-600" />
              </div>
              <h2 className="text-2xl font-semibold mb-2">Welcome Back</h2>
              <p className="text-gray-600 mb-8">Sign in to continue</p>
              <button
                onClick={handleLogin}
                className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-8 py-3 rounded-lg transition-colors flex items-center gap-2 mx-auto"
              >
                <LogIn className="w-5 h-5" />
                Sign in with Keycloak
              </button>
              
              {/* Debug Info - Remove in production */}
              <div className="mt-8 text-left bg-gray-50 rounded-lg p-4">
                <p className="text-xs font-mono text-gray-600">Debug Info:</p>
                <p className="text-xs font-mono">logIn available: {logIn ? '✓' : '✗'}</p>
                <p className="text-xs font-mono">isAuthenticated: {isAuthenticated ? '✓' : '✗'}</p>
                <p className="text-xs font-mono">token: {token ? 'present' : 'none'}</p>
              </div>
            </div>
          )}

          {/* Authenticated */}
          {isAuthenticated && (
            <div>
              {/* User Header */}
              <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-8 text-white">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center text-2xl font-bold">
                      {username.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <h2 className="text-2xl font-bold">{username}</h2>
                      {email && (
                        <div className="flex items-center gap-2 mt-1 text-blue-100">
                          <Mail className="w-4 h-4" />
                          {email}
                        </div>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="bg-white/20 hover:bg-white/30 px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
                  >
                    <LogOut className="w-4 h-4" />
                    Logout
                  </button>
                </div>
              </div>

              {/* Token Info */}
              <div className="p-8 space-y-6">
                
                {/* Token Status */}
                <div>
                  <div className="flex items-center gap-2 mb-3">
                    <Key className="w-5 h-5 text-gray-700" />
                    <h3 className="text-lg font-semibold">Token Status</h3>
                  </div>
                  <div className="bg-gray-50 rounded-lg p-4 space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">Access Token:</span>
                      <div className="flex items-center gap-2">
                        <code className="text-xs bg-white px-2 py-1 rounded border">
                          {token?.substring(0, 20)}...
                        </code>
                        <button
                          onClick={copyToken}
                          className="p-1 hover:bg-gray-200 rounded"
                        >
                          {copied ? <Check className="w-4 h-4 text-green-600" /> : <Copy className="w-4 h-4" />}
                        </button>
                      </div>
                    </div>

                    {tokenData?.exp && (
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-600">Time Remaining:</span>
                        <span className="text-sm font-semibold text-blue-600">
                          {getTimeRemaining()}
                        </span>
                      </div>
                    )}

                    {tokenData?.iat && (
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-600">Issued At:</span>
                        <span className="text-sm">
                          {new Date(tokenData.iat * 1000).toLocaleTimeString()}
                        </span>
                      </div>
                    )}

                    {tokenData?.scope && (
                      <div>
                        <span className="text-sm text-gray-600 block mb-2">Scopes:</span>
                        <div className="flex flex-wrap gap-2">
                          {tokenData.scope.split(' ').map((scope, i) => (
                            <span
                              key={i}
                              className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded-full"
                            >
                              {scope}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>

                {/* Token Data Toggle */}
                <div>
                  <button
                    onClick={() => setShowToken(!showToken)}
                    className="text-blue-600 hover:text-blue-700 font-medium text-sm flex items-center gap-2"
                  >
                    {showToken ? "Hide" : "Show"} Token Details
                  </button>
                  
                  {showToken && tokenData && (
                    <pre className="mt-3 bg-gray-900 text-green-400 p-4 rounded-lg overflow-auto text-xs max-h-80">
                      {JSON.stringify(tokenData, null, 2)}
                    </pre>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <p className="text-center text-gray-600 text-sm mt-6">
          Powered by Keycloak & react-oauth2-code-pkce
        </p>
      </div>
    </div>
  );
}

export default App;