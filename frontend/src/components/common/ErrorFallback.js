import React from 'react';
import { motion } from 'framer-motion';
import { Button } from 'react-bootstrap';
import './ErrorFallback.css';

const ErrorFallback = ({ error, resetErrorBoundary, errorInfo }) => {
  const handleReportError = () => {
    // TODO: Implement error reporting to logging service
    console.error('Error reported:', { error, errorInfo });
  };

  return (
    <div className="error-fallback-container">
      <motion.div
        initial={{ opacity: 0, scale: 0.8, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="error-fallback-content"
      >
        <div className="error-icon">⚠️</div>
        <h2 className="error-title">Oops! Something went wrong</h2>
        <p className="error-message">
          We encountered an unexpected error. Our team has been notified and is working on a fix.
        </p>

        {error && (
          <details className="error-details">
            <summary>Technical Details</summary>
            <div className="error-info">
              <div className="error-section">
                <strong>Error:</strong>
                <pre className="error-stack">{error.message}</pre>
              </div>
              {error.stack && (
                <div className="error-section">
                  <strong>Stack Trace:</strong>
                  <pre className="error-stack">{error.stack}</pre>
                </div>
              )}
              {errorInfo && errorInfo.componentStack && (
                <div className="error-section">
                  <strong>Component Stack:</strong>
                  <pre className="error-stack">{errorInfo.componentStack}</pre>
                </div>
              )}
            </div>
          </details>
        )}

        <div className="error-actions">
          <Button
            variant="primary"
            onClick={resetErrorBoundary}
            className="me-2"
          >
            🔄 Try Again
          </Button>
          <Button
            variant="outline-secondary"
            onClick={() => window.location.reload()}
            className="me-2"
          >
            ↻ Reload Page
          </Button>
          <Button
            variant="outline-info"
            onClick={handleReportError}
            size="sm"
          >
            📋 Report Issue
          </Button>
        </div>

        <div className="error-help">
          <p className="error-help-text">
            If this problem persists, please contact our support team with the error details above.
          </p>
        </div>
      </motion.div>
    </div>
  );
};

export default ErrorFallback;
