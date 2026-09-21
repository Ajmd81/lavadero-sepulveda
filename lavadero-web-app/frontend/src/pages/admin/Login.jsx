import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LogIn, Eye, EyeOff, AlertCircle, CheckCircle, Clock } from 'lucide-react';
import '../../login.css';

const Login = () => {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [errorType, setErrorType] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [focusedField, setFocusedField] = useState(null);
  const [bloqueadoHasta, setBloqueadoHasta] = useState(null);
  const [countdown, setCountdown] = useState(0);
  const passwordInputRef = useRef(null);
  const { login } = useAuth();
  const navigate = useNavigate();

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

  const renderPasswordSponges = () => {
    if (showPassword) {
      return credentials.password;
    }
    return Array(credentials.password.length)
      .fill('🧽')
      .join('');
  };

  return (
    <div className="login-wrapper">
      {/* IMAGEN DE FONDO DEL COCHE LAVADO */}
      <div className="audi-background"></div>

      {/* BURBUJAS FLOTANTES (30 BURBUJAS) */}
      <div className="bubble-container">
        <div className="bubble bubble-1"></div>
        <div className="bubble bubble-2"></div>
        <div className="bubble bubble-3"></div>
        <div className="bubble bubble-4"></div>
        <div className="bubble bubble-5"></div>
        <div className="bubble bubble-6"></div>
        <div className="bubble bubble-7"></div>
        <div className="bubble bubble-8"></div>
        <div className="bubble bubble-9"></div>
        <div className="bubble bubble-10"></div>
        <div className="bubble bubble-11"></div>
        <div className="bubble bubble-12"></div>
        <div className="bubble bubble-13"></div>
        <div className="bubble bubble-14"></div>
        <div className="bubble bubble-15"></div>
        <div className="bubble bubble-16"></div>
        <div className="bubble bubble-17"></div>
        <div className="bubble bubble-18"></div>
        <div className="bubble bubble-19"></div>
        <div className="bubble bubble-20"></div>
        <div className="bubble bubble-21"></div>
        <div className="bubble bubble-22"></div>
        <div className="bubble bubble-23"></div>
        <div className="bubble bubble-24"></div>
        <div className="bubble bubble-25"></div>
        <div className="bubble bubble-26"></div>
        <div className="bubble bubble-27"></div>
        <div className="bubble bubble-28"></div>
        <div className="bubble bubble-29"></div>
        <div className="bubble bubble-30"></div>
      </div>

      {/* CONTENEDOR FORMULARIO TRANSPARENTE */}
      <div className="login-container">
        <div className="form-card">
          <div className="text-center mb-6 animate-fadeInUp">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-blue-500 to-blue-700 rounded-full mb-4 shadow-lg">
              <img src="/assets/icons/logo_crm.png" alt="Logo Lavadero" className="w-full h-full object-contain drop-shadow-lg p-2" fetchPriority='high' loading='eager' />
            </div>
            <h1 className="text-3xl font-bold bg-gradient-to-r from-slate-900 to-blue-700 bg-clip-text text-transparent mb-1">Lavadero Sepúlveda</h1>
            <p className="text-slate-600 font-medium text-sm">CRM Administrativo</p>
          </div>

          {error && (
            <div className="mb-4 animate-shake">
              {errorType === 'rate_limit' ? (
                <div className="bg-gradient-to-r from-orange-50 to-orange-100 border-l-4 border-orange-500 rounded p-3 flex items-start gap-2 shadow-sm">
                  <Clock className="text-orange-500 flex-shrink-0 mt-0.5" size={18} />
                  <div className="flex-1 text-xs">
                    <p className="font-semibold text-orange-900">Acceso bloqueado</p>
                    <p className="text-orange-700 mt-0.5">{error}</p>
                    {countdown > 0 && (
                      <div className="mt-2 flex items-center gap-2">
                        <div className="h-1 bg-orange-200 rounded-full flex-1 overflow-hidden">
                          <div className="h-full bg-orange-500 rounded-full transition-all duration-1000" style={{ width: `${Math.min(100, (countdown / 60) * 100)}%` }} />
                        </div>
                        <span className="text-orange-800 font-mono font-bold text-xs min-w-[2rem]">{formatCountdown(countdown)}</span>
                      </div>
                    )}
                  </div>
                </div>
              ) : (
                <div className="bg-gradient-to-r from-red-50 to-red-100 border-l-4 border-red-500 rounded p-3 flex items-start gap-2 shadow-sm">
                  <AlertCircle className="text-red-500 flex-shrink-0 mt-0.5" size={18} />
                  <div className="text-xs">
                    <p className="font-semibold text-red-900">Error de acceso</p>
                    <p className="text-red-700 mt-0.5">{error}</p>
                  </div>
                </div>
              )}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-2 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-blue-500"></span>
                Usuario
              </label>
              <div className={`relative transition-all duration-300 ${focusedField === 'username' ? 'scale-105' : 'scale-100'}`}>
                <input type="text" value={credentials.username} onChange={(e) => setCredentials({ ...credentials, username: e.target.value })} onFocus={() => setFocusedField('username')} onBlur={() => setFocusedField(null)} disabled={!!estaBloqueado} className="w-full px-4 py-2.5 text-sm bg-white bg-opacity-90 border-2 border-slate-200 rounded-lg transition-all duration-300 text-slate-900 placeholder-slate-400 focus:border-blue-500 focus:shadow-lg focus:shadow-blue-500/20 focus:bg-white outline-none disabled:opacity-50 disabled:cursor-not-allowed" placeholder="Usuario" required />
                {credentials.username && !estaBloqueado && <CheckCircle className="absolute right-3 top-1/2 -translate-y-1/2 text-green-500" size={18} />}
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-2 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-blue-500"></span>
                Contraseña
              </label>
              <div className={`relative transition-all duration-300 ${focusedField === 'password' ? 'scale-105' : 'scale-100'}`}>
                <input ref={passwordInputRef} type="password" value={credentials.password} onChange={(e) => setCredentials({ ...credentials, password: e.target.value })} disabled={!!estaBloqueado} className="sr-only" placeholder="Contraseña" required />
                <div onClick={() => passwordInputRef.current?.focus()} onFocus={() => setFocusedField('password')} onBlur={() => setFocusedField(null)} className="w-full px-4 py-2.5 text-lg bg-white bg-opacity-90 border-2 border-slate-200 rounded-lg transition-all duration-300 text-slate-900 placeholder-slate-400 focus:border-blue-500 focus:shadow-lg focus:shadow-blue-500/20 focus:bg-white outline-none pr-12 disabled:opacity-50 disabled:cursor-not-allowed cursor-text">
                  <span className="tracking-wide">{credentials.password.length > 0 ? renderPasswordSponges() : ''}</span>
                  {credentials.password.length === 0 && <span className="text-slate-400 text-sm">Contraseña</span>}
                </div>
                <button type="button" onClick={() => setShowPassword(!showPassword)} disabled={!!estaBloqueado} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-blue-600 transition-colors p-1 disabled:opacity-50 disabled:cursor-not-allowed" title={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              <p className="text-xs text-slate-500 mt-1 ml-1">
                {showPassword && credentials.password.length > 0 && '🔓 Visible'}
                {!showPassword && credentials.password.length > 0 && `🧽 ${credentials.password.length} car.`}
              </p>
            </div>

            <button type="submit" disabled={loading || !!estaBloqueado} className="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 disabled:from-slate-400 disabled:to-slate-500 disabled:cursor-not-allowed text-white font-bold py-3 px-6 text-sm rounded-lg transition-all duration-300 transform hover:scale-105 shadow-md hover:shadow-blue-600/50 disabled:shadow-none mt-6 relative group/btn overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-r from-blue-400 via-blue-500 to-blue-600 opacity-0 group-hover/btn:opacity-100 transition-opacity duration-300"></div>
              <span className="relative flex items-center justify-center gap-2">
                {loading ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                    Iniciando...
                  </>
                ) : estaBloqueado ? (
                  <>
                    <Clock size={16} />
                    {formatCountdown(countdown)}
                  </>
                ) : (
                  <>
                    <LogIn size={16} />
                    Iniciar Sesión
                  </>
                )}
              </span>
            </button>
          </form>

          <div className="mt-4 pt-4 border-t border-slate-200">
            <p className="text-center text-xs text-slate-500">© 2025 Lavadero Sepúlveda</p>
          </div>
        </div>
      </div>

      {loading && (
        <div className="mt-6 flex justify-center relative z-20">
          <div className="flex gap-1">
            <div className="w-2 h-2 rounded-full bg-blue-600 animate-bounce" style={{ animationDelay: '0s' }}></div>
            <div className="w-2 h-2 rounded-full bg-blue-600 animate-bounce" style={{ animationDelay: '0.15s' }}></div>
            <div className="w-2 h-2 rounded-full bg-blue-600 animate-bounce" style={{ animationDelay: '0.3s' }}></div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Login;