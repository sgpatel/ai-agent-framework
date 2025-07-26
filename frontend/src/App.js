import React, { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import { HelmetProvider } from 'react-helmet-async';
import { ErrorBoundary } from 'react-error-boundary';
import { Toaster } from 'react-hot-toast';

// Components
import Navigation from './components/Navigation';
import HomePage from './components/HomePage';
import Login from './components/Login';
import Register from './components/Register';
import AgentDashboard from './components/AgentDashboard';
import TaskForm from './components/TaskForm';
import TaskHistory from './components/TaskHistory';
import SystemMetrics from './components/SystemMetrics';
import SystemMonitoring from './components/SystemMonitoring';
import PluginManager from './components/PluginManager';
import StockAnalyzer from './components/StockAnalyzer';
import IntelligentDashboard from './components/IntelligentDashboard';
import GPT4AllChat from './components/GPT4AllChat';
import ContextManager from './components/ContextManager';
import PerplexitySearch from './components/PerplexitySearch';
import ErrorFallback from './components/common/ErrorFallback';

// Context
import { AppContextProvider, useAppContext } from './context/AppContext';

// Styles
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/themes.css';
import './styles/components.css';
import './App.css';

// Theme Manager Component
const ThemeManager = () => {
  const { theme } = useAppContext();

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  return null;
};

// Main App Layout
function AppLayout() {
  return (
    <div className="app">
      <ThemeManager />
      <Navigation />
      <main className="main-content">
        <Container fluid className="h-100">
          <ErrorBoundary
            FallbackComponent={ErrorFallback}
            onError={(error, errorInfo) => {
              console.error('Application Error:', error, errorInfo);
            }}
          >
            <Routes>
              <Route path='/' element={<HomePage />} />
              <Route path='/dashboard' element={<AgentDashboard />} />
              <Route path='/context-manager' element={<ContextManager />} />
              <Route path='/intelligent-dashboard' element={<IntelligentDashboard />} />
              <Route path='/task-management' element={<TaskForm />} />
              <Route path='/plugin-manager' element={<PluginManager />} />
              <Route path='/ai-chat' element={<GPT4AllChat />} />
              <Route path='/search' element={<PerplexitySearch />} />
              <Route path='/tasks/new' element={<TaskForm />} />
              <Route path='/tasks/history' element={<TaskHistory />} />
              <Route path='/metrics' element={<SystemMetrics />} />
              <Route path='/monitoring' element={<SystemMonitoring />} />
              <Route path='/stocks' element={<StockAnalyzer />} />
              <Route path='/login' element={<Login />} />
              <Route path='/register' element={<Register />} />
            </Routes>
          </ErrorBoundary>
        </Container>
      </main>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: 'var(--bg-secondary)',
            color: 'var(--text-primary)',
            border: '1px solid var(--border-primary)',
            borderRadius: 'var(--radius-lg)',
            boxShadow: 'var(--shadow-lg)',
          },
        }}
      />
    </div>
  );
}

function App() {
  return (
    <HelmetProvider>
      <AppContextProvider>
        <AppLayout />
      </AppContextProvider>
    </HelmetProvider>
  );
}

export default App;
