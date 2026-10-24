import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

function DetailIcon({ type }) {
  const paths = {
    feels: <><path d="M14 14.76V5a2 2 0 0 0-4 0v9.76a4 4 0 1 0 4 0Z" /><path d="M12 11v6" /></>,
    humidity: <><path d="M12 22a7 7 0 0 0 7-7c0-4-7-13-7-13S5 11 5 15a7 7 0 0 0 7 7Z" /><path d="M9 16a3 3 0 0 0 3 3" /></>,
    wind: <><path d="M3 8h12a3 3 0 1 0-3-3" /><path d="M2 12h17a3 3 0 1 1-3 3" /><path d="M4 16h5a3 3 0 1 1-3 3" /></>,
    pressure: <><circle cx="12" cy="12" r="9" /><path d="M12 12 16 8M7 16h10" /></>,
  };

  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      {paths[type]}
    </svg>
  );
}

function App() {
  const [city, setCity] = useState('Buenos Aires');
  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function searchWeather(searchCity = city) {
    const trimmedCity = searchCity.trim();
    if (!trimmedCity) {
      setError('Escribe el nombre de una ciudad para buscar.');
      setWeather(null);
      return;
    }

    setCity(trimmedCity);
    setLoading(true);
    setError('');
    try {
      const response = await fetch(`/api/weather?city=${encodeURIComponent(trimmedCity)}`);
      const result = await response.json();
      if (!response.ok) {
        throw new Error(result.error || 'No pudimos consultar el clima. Inténtalo de nuevo.');
      }
      setWeather(result);
    } catch (requestError) {
      setWeather(null);
      setError(requestError.message || 'No pudimos conectar con el servidor.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    searchWeather('Buenos Aires');
  }, []);

  function handleSubmit(event) {
    event.preventDefault();
    searchWeather();
  }

  const dateLabel = new Intl.DateTimeFormat('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  }).format(new Date());

  return (
    <main className="page">
      <div className="ambient ambient-one" />
      <div className="ambient ambient-two" />
      <header className="topbar">
        <a className="brand" href="/" aria-label="Clima, inicio">
          <span className="brand-mark" aria-hidden="true">☼</span>
          <span>clima<span className="brand-dot">.</span></span>
        </a>
        <span className="topbar-note"><span className="live-dot" /> Clima en tiempo real</span>
      </header>

      <section className="content" aria-labelledby="page-title">
        <div className="intro">
          <h1 id="page-title">El tiempo, <span>hoy</span></h1>
          <p className="subtitle">Encuentra las condiciones actuales de cualquier ciudad.</p>

          <form className="search" onSubmit={handleSubmit} role="search">
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              strokeWidth="1.8" strokeLinecap="round">
              <circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" />
            </svg>
            <label className="sr-only" htmlFor="city-search">Buscar ciudad</label>
            <input
              id="city-search"
              value={city}
              onChange={(event) => setCity(event.target.value)}
              placeholder="Busca una ciudad..."
              autoComplete="off"
            />
            <button type="submit" disabled={loading}>
              {loading ? <span className="button-spinner" /> : 'Buscar'}
            </button>
          </form>
        </div>

        <section className="weather-card" aria-live="polite" aria-busy={loading}>
          <div className="card-top">
            <div>
              <p className="card-label">CLIMA ACTUAL</p>
              <p className="date">{dateLabel}</p>
            </div>
            <span className="location">
              <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 10c0 5-8 12-8 12S4 15 4 10a8 8 0 1 1 16 0Z" />
                <circle cx="12" cy="10" r="2.5" />
              </svg>
              {weather ? `${weather.city}${weather.country ? `, ${weather.country}` : ''}` : city}
            </span>
          </div>

          {loading && !weather ? (
            <div className="message-state"><span className="large-spinner" />Consultando el clima...</div>
          ) : error ? (
            <div className="message-state error-state" role="alert">
              <span className="error-symbol">!</span>
              <div><strong>No pudimos cargar el clima</strong><p>{error}</p></div>
            </div>
          ) : weather ? (
            <>
              <div className="current-weather">
                <div className="temperature-wrap">
                  <div className="temperature">{Math.round(weather.temperature)}<span>°</span></div>
                  <p className="condition">{weather.description}</p>
                </div>
                <img
                  className="weather-art"
                  src={`https://openweathermap.org/img/wn/${weather.icon}@4x.png`}
                  alt={weather.description}
                />
              </div>
              <div className="weather-divider" />
              <div className="details">
                <div className="detail">
                  <span className="detail-icon"><DetailIcon type="feels" /></span>
                  <div><span className="detail-label">Sensación térmica</span>
                    <strong>{Math.round(weather.feelsLike)}°</strong></div>
                </div>
                <div className="detail">
                  <span className="detail-icon"><DetailIcon type="humidity" /></span>
                  <div><span className="detail-label">Humedad</span>
                    <strong>{weather.humidity}%</strong></div>
                </div>
                <div className="detail">
                  <span className="detail-icon"><DetailIcon type="wind" /></span>
                  <div><span className="detail-label">Viento</span>
                    <strong>{weather.windSpeed} <small>m/s</small></strong></div>
                </div>
                <div className="detail">
                  <span className="detail-icon"><DetailIcon type="pressure" /></span>
                  <div><span className="detail-label">Presión</span>
                    <strong>{weather.pressure} <small>hPa</small></strong></div>
                </div>
              </div>
            </>
          ) : (
            <div className="message-state">Busca una ciudad para ver el clima actual.</div>
          )}
        </section>

        <p className="footnote">Datos meteorológicos proporcionados por OpenWeather</p>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
