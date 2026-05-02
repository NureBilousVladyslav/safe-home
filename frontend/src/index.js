import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './components/app';
import './configs/locale';
import { CookiesProvider } from 'react-cookie';
import { BrowserRouter as Router } from 'react-router-dom';
import { AuthProvider } from './contexts/auth-context';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <CookiesProvider>
      <Router>
        <AuthProvider>
          <App />
        </AuthProvider>
      </Router>
    </CookiesProvider>
  </React.StrictMode>
);
