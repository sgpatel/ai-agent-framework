// PropTypes and validation utilities
import React from 'react';
import PropTypes from 'prop-types';
import { ErrorBoundary } from 'react-error-boundary';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorFallback from '../components/common/ErrorFallback';

// Common PropTypes
export const AgentPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  type: PropTypes.string.isRequired,
  status: PropTypes.oneOf(['active', 'inactive', 'error', 'loading']).isRequired,
  description: PropTypes.string,
  capabilities: PropTypes.arrayOf(PropTypes.string),
  lastUpdated: PropTypes.string,
});

export const TaskPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  description: PropTypes.string,
  status: PropTypes.oneOf(['pending', 'running', 'completed', 'failed']).isRequired,
  agentId: PropTypes.string,
  createdAt: PropTypes.string.isRequired,
  updatedAt: PropTypes.string,
  result: PropTypes.object,
});

export const MetricsPropType = PropTypes.shape({
  cpu: PropTypes.number,
  memory: PropTypes.number,
  activeAgents: PropTypes.number,
  completedTasks: PropTypes.number,
  errorRate: PropTypes.number,
});

// Validation utilities
export const validateStockSymbol = symbol => {
  if (!symbol || typeof symbol !== 'string') {
    return false;
  }
  return /^[A-Z]{1,5}$/.test(symbol.toUpperCase());
};

export const validateApiResponse = response => {
  return response && typeof response === 'object' && !response.error;
};

export const validateFormData = (data, requiredFields) => {
  const errors = {};

  requiredFields.forEach(field => {
    if (!data[field] || (typeof data[field] === 'string' && data[field].trim() === '')) {
      errors[field] = `${field} is required`;
    }
  });

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};

// Error boundary HOC
export const withErrorBoundary = (Component, fallbackComponent = ErrorFallback) => {
  const ErrorBoundaryWrapper = props => {
    return (
      <ErrorBoundary fallback={fallbackComponent}>
        <Component {...props} />
      </ErrorBoundary>
    );
  };

  ErrorBoundaryWrapper.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;
  return ErrorBoundaryWrapper;
};

// Loading wrapper HOC
export const withLoading = Component => {
  const LoadingWrapper = ({ loading, ...props }) => {
    if (loading) {
      return <LoadingSpinner />;
    }
    return <Component {...props} />;
  };

  LoadingWrapper.propTypes = {
    loading: PropTypes.bool,
  };

  LoadingWrapper.displayName = `withLoading(${Component.displayName || Component.name})`;
  return LoadingWrapper;
};
