import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAppContext } from '../context/AppContext';
import { Card, CardHeader, CardTitle, CardBody, Button, Input } from './common/UIComponents';
import toast from 'react-hot-toast';
import './Auth.css';

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAppContext();
  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
    rememberMe: false,
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));

    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.usernameOrEmail.trim()) {
      newErrors.usernameOrEmail = 'Username or email is required';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (data.success) {
        // Store token and user info
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.user));

        // Update app context
        if (login) {
          login(data.user, data.token);
        }

        toast.success('Login successful! Welcome back.');
        navigate('/dashboard');
      } else {
        toast.error(data.message || 'Login failed');
        setErrors({ general: data.message || 'Login failed' });
      }
    } catch (error) {
      console.error('Login error:', error);
      toast.error('An error occurred during login');
      setErrors({ general: 'An error occurred during login' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="auth-card-wrapper"
      >
        <Card className="auth-card">
          <CardHeader className="auth-card-header">
            <div className="auth-brand">
              <span className="auth-icon">🧠</span>
              <CardTitle className="auth-title">Welcome Back</CardTitle>
            </div>
            <p className="auth-subtitle">Sign in to your AI Agent Framework account</p>
          </CardHeader>

          <CardBody className="auth-card-body">
            <form onSubmit={handleSubmit} className="auth-form">
              {errors.general && (
                <div className="auth-error-banner">
                  {errors.general}
                </div>
              )}

              <div className="form-group">
                <label htmlFor="usernameOrEmail" className="form-label">
                  Username or Email
                </label>
                <Input
                  id="usernameOrEmail"
                  name="usernameOrEmail"
                  type="text"
                  value={formData.usernameOrEmail}
                  onChange={handleChange}
                  placeholder="Enter your username or email"
                  error={!!errors.usernameOrEmail}
                  disabled={loading}
                />
                {errors.usernameOrEmail && (
                  <span className="form-error">{errors.usernameOrEmail}</span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="password" className="form-label">
                  Password
                </label>
                <Input
                  id="password"
                  name="password"
                  type="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Enter your password"
                  error={!!errors.password}
                  disabled={loading}
                />
                {errors.password && (
                  <span className="form-error">{errors.password}</span>
                )}
              </div>

              <div className="form-group form-checkbox-group">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="rememberMe"
                    checked={formData.rememberMe}
                    onChange={handleChange}
                    disabled={loading}
                  />
                  <span className="checkbox-text">Remember me</span>
                </label>
              </div>

              <Button
                type="submit"
                variant="primary"
                size="lg"
                loading={loading}
                disabled={loading}
                className="auth-submit-btn"
              >
                {loading ? 'Signing In...' : 'Sign In'}
              </Button>
            </form>

            <div className="auth-divider">
              <span>or</span>
            </div>

            <div className="auth-links">
              <p>
                Don&apos;t have an account?{' '}
                <Link to="/register" className="auth-link">
                  Create one here
                </Link>
              </p>
              <Link to="/forgot-password" className="auth-link">
                Forgot your password?
              </Link>
            </div>
          </CardBody>
        </Card>
      </motion.div>
    </div>
  );
};

export default Login;
