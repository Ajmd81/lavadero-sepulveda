import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LogIn, Eye, EyeOff, AlertCircle, CheckCircle, Clock } from 'lucide-react';

const Login = () => {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [errorType, setErrorType] = useState(null); // 'rate_limit' | 'credentials' | 'network'
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [focusedField, setFocusedField] = useState(null);
  const [bloqueadoHasta, setBloqueadoHasta] = useState(null); // timestamp ms
  const [countdown, setCountdown] = useState(0);
  const { login } = useAuth();
  const navigate = useNavigate();

  // Cuenta atrás cuando hay rate limit activo
  useEffect(() => {
    if (!bloqueadoHasta) return;

    const intervalo = setInterval(() => {
      const restante = Math.ceil((bloqueadoHasta - Date.now()) / 1000);
      if (restante <= 0) {
        setBloqueadoHasta(null);
        setCountdown(0);
        setError('');
        setErrorType(null);
        clearInterval(intervalo);
      } else {
        setCountdown(restante);
      }
    }, 1000);

    return () => clearInterval(intervalo);
  }, [bloqueadoHasta]);

  const estaBloqueado = bloqueadoHasta && Date.now() < bloqueadoHasta;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (estaBloqueado) return;

    setError('');
    setErrorType(null);
    setLoading(true);

    const result = await login(credentials);

    if (result.success) {
      navigate('/admin');
    } else {
      if (result.status === 429) {
        const segundos = result.retryAfter ?? 60;
        setBloqueadoHasta(Date.now() + segundos * 1000);
        setCountdown(segundos);
        setErrorType('rate_limit');
      } else if (result.status === 401) {
        setErrorType('credentials');
      } else {
        setErrorType('network');
      }
      setError(result.error);
    }

    setLoading(false);
  };

  const formatCountdown = (s) => {
    if (s >= 60) return `${Math.floor(s / 60)}m ${s % 60}s`;
    return `${s}s`;
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-slate-100 flex items-center justify-center p-4 relative overflow-hidden">
      {/* Elementos decorativos de fondo */}
      <div className="absolute top-0 left-0 w-96 h-96 bg-gradient-to-br from-blue-400 to-blue-600 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse"></div>
      <div className="absolute bottom-0 right-0 w-96 h-96 bg-gradient-to-br from-purple-400 to-blue-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse" style={{ animationDelay: '2s' }}></div>

      <div className="relative w-full max-w-2xl">
        <div className="bg-white rounded-2xl shadow-2xl p-12 border border-slate-100 backdrop-blur-xl bg-opacity-95 relative group">
          <div className="absolute -inset-0.5 bg-gradient-to-r from-blue-400 via-purple-400 to-blue-400 rounded-2xl opacity-0 group-hover:opacity-10 transition-opacity duration-500 blur-lg"></div>

          <div className="relative">
            {/* Header */}
            <div className="text-center mb-12 animate-fadeInUp">
              <div className="inline-flex items-center justify-center w-32 h-32 bg-gradient-to-br from-blue-500 to-blue-700 rounded-2xl mb-8 shadow-lg shadow-blue-500/50 relative group/logo p-3">
                <div className="absolute inset-0 bg-gradient-to-r from-blue-400 to-purple-500 rounded-2xl opacity-0 group-hover/logo:opacity-100 transition-opacity duration-300 blur-xl"></div>
                <img src="/assets/icons/logo_crm.png" alt="Logo Lavadero" className="relative w-full h-full object-contain drop-shadow-lg" />
              </div>
              <h1 className="text-5xl font-bold bg-gradient-to-r from-slate-900 via-blue-600 to-blue-800 bg-clip-text text-transparent mb-3">
                Lavadero Sepúlveda
              </h1>
              <p className="text-slate-600 font-medium text-lg">CRM Administrativo</p>
            </div>

            {/* Error / Bloqueo */}
            {error && (
              <div className="mb-8 animate-shake">
                {errorType === 'rate_limit' ? (
                  // Bloqueo por fuerza bruta — alerta naranja con cuenta atrás
                  <div className="bg-gradient-to-r from-orange-50 to-orange-100 border-l-4 border-orange-500 rounded-lg p-5 flex items-start gap-3 shadow-md">
                    <Clock className="text-orange-500 flex-shrink-0 mt-0.5" size={24} />
                    <div className="flex-1">
                      <p className="font-semibold text-orange-900 text-lg">Acceso temporalmente bloqueado</p>
                      <p className="text-orange-700 text-base mt-1">{error}</p>
                      {countdown > 0 && (
                        <div className="mt-3 flex items-center gap-2">
                          <div className="h-2 bg-orange-200 rounded-full flex-1 overflow-hidden">
                            <div
                              className="h-full bg-orange-500 rounded-full transition-all duration-1000"
                              style={{ width: `${Math.min(100, (countdown / 60) * 100)}%` }}
                            />
                          </div>
                          <span className="text-orange-800 font-mono font-bold text-sm min-w-[3rem] text-right">
                            {formatCountdown(countdown)}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                ) : (
                  // Error normal de credenciales o red
                  <div className="bg-gradient-to-r from-red-50 to-red-100 border-l-4 border-red-500 rounded-lg p-5 flex items-start gap-3 shadow-md">
                    <AlertCircle className="text-red-500 flex-shrink-0 mt-0.5" size={24} />
                    <div>
                      <p className="font-semibold text-red-900 text-lg">Error de acceso</p>
                      <p className="text-red-700 text-base mt-1">{error}</p>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Formulario */}
            <form onSubmit={handleSubmit} className="space-y-8">
              {/* Campo Usuario */}
              <div className="group">
                <label className="block text-xl font-bold text-slate-700 mb-5 flex items-center gap-3">
                  <span className="w-2 h-2 rounded-full bg-gradient-to-r from-blue-500 to-purple-500"></span>
                  Usuario
                </label>
                <div className={`relative transition-all duration-300 ${focusedField === 'username' ? 'scale-105' : 'scale-100'}`}>
                  <input
                    type="text"
                    value={credentials.username}
                    onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
                    onFocus={() => setFocusedField('username')}
                    onBlur={() => setFocusedField(null)}
                    disabled={!!estaBloqueado}
                    className="w-full px-7 py-5 text-xl bg-slate-50 border-2 border-slate-200 rounded-xl transition-all duration-300 text-slate-900 placeholder-slate-400 focus:border-blue-500 focus:shadow-lg focus:shadow-blue-500/20 focus:bg-white outline-none disabled:opacity-50 disabled:cursor-not-allowed"
                    placeholder="Ingresa tu usuario"
                    required
                  />
                  {credentials.username && !estaBloqueado && (
                    <CheckCircle className="absolute right-6 top-1/2 -translate-y-1/2 text-green-500" size={28} />
                  )}
                </div>
              </div>

              {/* Campo Contraseña */}
              <div className="group">
                <label className="block text-xl font-bold text-slate-700 mb-5 flex items-center gap-3">
                  <span className="w-2 h-2 rounded-full bg-gradient-to-r from-blue-500 to-purple-500"></span>
                  Contraseña
                </label>
                <div className={`relative transition-all duration-300 ${focusedField === 'password' ? 'scale-105' : 'scale-100'}`}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={credentials.password}
                    onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
                    onFocus={() => setFocusedField('password')}
                    onBlur={() => setFocusedField(null)}
                    disabled={!!estaBloqueado}
                    className="w-full px-7 py-5 text-xl bg-slate-50 border-2 border-slate-200 rounded-xl transition-all duration-300 text-slate-900 placeholder-slate-400 focus:border-blue-500 focus:shadow-lg focus:shadow-blue-500/20 focus:bg-white outline-none pr-16 disabled:opacity-50 disabled:cursor-not-allowed"
                    placeholder="Ingresa tu contraseña"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-6 top-1/2 -translate-y-1/2 text-slate-500 hover:text-blue-600 transition-colors p-1"
                  >
                    {showPassword ? <EyeOff size={28} /> : <Eye size={28} />}
                  </button>
                </div>
              </div>

              {/* Botón */}
              <button
                type="submit"
                disabled={loading || !!estaBloqueado}
                className="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 disabled:from-slate-400 disabled:to-slate-500 disabled:cursor-not-allowed text-white font-bold py-5 px-8 text-xl rounded-xl transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-blue-600/50 disabled:shadow-none mt-8 relative group/btn overflow-hidden"
              >
                <div className="absolute inset-0 bg-gradient-to-r from-blue-400 via-blue-500 to-blue-600 opacity-0 group-hover/btn:opacity-100 transition-opacity duration-300"></div>
                <span className="relative flex items-center justify-center gap-3">
                  {loading ? (
                    <>
                      <div className="w-7 h-7 border-3 border-white/30 border-t-white rounded-full animate-spin"></div>
                      Iniciando sesión...
                    </>
                  ) : estaBloqueado ? (
                    <>
                      <Clock size={28} />
                      Bloqueado — {formatCountdown(countdown)}
                    </>
                  ) : (
                    <>
                      <LogIn size={28} />
                      Iniciar Sesión
                    </>
                  )}
                </span>
              </button>
            </form>

            {/* Footer */}
            <div className="mt-10 pt-8 border-t border-slate-200">
              <p className="text-center text-sm text-slate-500 mt-3">
                © 2025 Lavadero Sepúlveda - Todos los derechos reservados
              </p>
            </div>
          </div>
        </div>

        {/* Indicador de carga */}
        {loading && (
          <div className="mt-6 flex justify-center">
            <div className="flex gap-1">
              <div className="w-2 h-2 rounded-full bg-blue-600 animate-bounce" style={{ animationDelay: '0s' }}></div>
              <div className="w-2 h-2 rounded-full bg-blue-600 animate-bounce" style={{ animationDelay: '0.15s' }}></div>
              <div className="w-2 h-2 rounded-full bg-blue-600 animate-bounce" style={{ animationDelay: '0.3s' }}></div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Login;