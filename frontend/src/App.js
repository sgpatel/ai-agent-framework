import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import Navigation from './components/Navigation';
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
import { AppContextProvider } from './context/AppContext';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function App() {
    return (
        <AppContextProvider>
            <Navigation />
            <Container fluid className="main-content">
                <Routes>
                    <Route path="/" element={<Navigate to="/dashboard" replace />} />
                    <Route path="/dashboard" element={<AgentDashboard />} />
                    <Route path="/intelligent" element={<IntelligentDashboard />} />
                    <Route path="/ai-chat" element={<GPT4AllChat />} />
                    <Route path="/context" element={<ContextManager />} />
                    <Route path="/tasks/new" element={<TaskForm />} />
                    <Route path="/tasks/history" element={<TaskHistory />} />
                    <Route path="/metrics" element={<SystemMetrics />} />
                    <Route path="/monitoring" element={<SystemMonitoring />} />
                    <Route path="/plugins" element={<PluginManager />} />
                    <Route path="/stocks" element={<StockAnalyzer />} />
                </Routes>
            </Container>
        </AppContextProvider>
    );
}

export default App;
