import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAppContext } from '../context/AppContext';
import { Card, CardHeader, CardTitle, CardBody, Button, Input } from './common/UIComponents';
import toast from 'react-hot-toast';
import './Auth.css';

const Register = () => {
  const navigate = useNavigate();
  const { login } = useAppContext();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [step, setStep] = useState(1); // Multi-step form

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));

    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateStep1 = () => {
    const newErrors = {};

    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
    } else if (formData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateStep2 = () => {
    const newErrors = {};

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNext = async () => {
    if (step === 1 && validateStep1()) {
      // Check username and email availability
      try {
        const [usernameCheck, emailCheck] = await Promise.all([
          fetch(`http://localhost:8080/api/auth/check-username?username=${formData.username}`),
          fetch(`http://localhost:8080/api/auth/check-email?email=${formData.email}`),
        ]);

        const usernameData = await usernameCheck.json();
        const emailData = await emailCheck.json();

        const newErrors = {};
        if (!usernameData.available) {
          newErrors.username = 'Username is already taken';
        }
        if (!emailData.available) {
          newErrors.email = 'Email is already registered';
        }

        if (Object.keys(newErrors).length > 0) {
          setErrors(newErrors);
          return;
        }

        setStep(2);
      } catch (error) {
        console.error('Availability check error:', error);
        toast.error('Error checking availability');
      }
    }
  };

  const handleBack = () => {
    setStep(1);
    setErrors({});
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateStep2()) {
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
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

        toast.success('Registration successful! Welcome to AI Agent Framework.');
        navigate('/dashboard');
      } else {
        toast.error(data.message || 'Registration failed');
        setErrors({ general: data.message || 'Registration failed' });
      }
    } catch (error) {
      console.error('Registration error:', error);
      toast.error('An error occurred during registration');
      setErrors({ general: 'An error occurred during registration' });
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
        <Card className="auth-card register-card">
          <CardHeader className="auth-card-header">
            <div className="auth-brand">
              <span className="auth-icon">🚀</span>
              <CardTitle className="auth-title">
                {step === 1 ? 'Create Account' : 'Set Password'}
              </CardTitle>
            </div>
            <p className="auth-subtitle">
              {step === 1
                ? 'Join the AI Agent Framework community'
                : 'Secure your account with a strong password'
              }
            </p>

            {/* Progress indicator */}
            <div className="auth-progress">
              <div className={`progress-step ${step >= 1 ? 'active' : ''}`}>1</div>
              <div className="progress-line"></div>
              <div className={`progress-step ${step >= 2 ? 'active' : ''}`}>2</div>
            </div>
          </CardHeader>

          <CardBody className="auth-card-body">
            {step === 1 ? (
              // Step 1: Basic Information
              <div className="auth-form">
                {errors.general && (
                  <div className="auth-error-banner">
                    {errors.general}
                  </div>
                )}

                <div className="form-group">
                  <label htmlFor="username" className="form-label">
                    Username *
                  </label>
                  <Input
                    id="username"
                    name="username"
                    type="text"
                    value={formData.username}
                    onChange={handleChange}
                    placeholder="Choose a unique username"
                    error={!!errors.username}
                    disabled={loading}
                  />
                  {errors.username && (
                    <span className="form-error">{errors.username}</span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="email" className="form-label">
                    Email Address *
                  </label>
                  <Input
                    id="email"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="Enter your email address"
                    error={!!errors.email}
                    disabled={loading}
                  />
                  {errors.email && (
                    <span className="form-error">{errors.email}</span>
                  )}
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="firstName" className="form-label">
                      First Name
                    </label>
                    <Input
                      id="firstName"
                      name="firstName"
                      type="text"
                      value={formData.firstName}
                      onChange={handleChange}
                      placeholder="First name"
                      disabled={loading}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="lastName" className="form-label">
                      Last Name
                    </label>
                    <Input
                      id="lastName"
                      name="lastName"
                      type="text"
                      value={formData.lastName}
                      onChange={handleChange}
                      placeholder="Last name"
                      disabled={loading}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="phoneNumber" className="form-label">
                    Phone Number
                  </label>
                  <Input
                    id="phoneNumber"
                    name="phoneNumber"
                    type="tel"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    placeholder="Optional phone number"
                    disabled={loading}
                  />
                </div>

                <Button
                  type="button"
                  onClick={handleNext}
                  variant="primary"
                  size="lg"
                  disabled={loading}
                  className="auth-submit-btn"
                >
                  Next Step →
                </Button>
              </div>
            ) : (
              // Step 2: Password Setup
              <form onSubmit={handleSubmit} className="auth-form">
                {errors.general && (
                  <div className="auth-error-banner">
                    {errors.general}
                  </div>
                )}

                <div className="form-group">
                  <label htmlFor="password" className="form-label">
                    Password *
                  </label>
                  <Input
                    id="password"
                    name="password"
                    type="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="Create a strong password"
                    error={!!errors.password}
                    disabled={loading}
                  />
                  {errors.password && (
                    <span className="form-error">{errors.password}</span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="confirmPassword" className="form-label">
                    Confirm Password *
                  </label>
                  <Input
                    id="confirmPassword"
                    name="confirmPassword"
                    type="password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="Confirm your password"
                    error={!!errors.confirmPassword}
                    disabled={loading}
                  />
                  {errors.confirmPassword && (
                    <span className="form-error">{errors.confirmPassword}</span>
                  )}
                </div>

                <div className="password-requirements">
                  <p className="requirements-title">Password must contain:</p>
                  <ul className="requirements-list">
                    <li className={formData.password.length >= 6 ? 'valid' : ''}>
                      At least 6 characters
                    </li>
                    <li className={/[A-Z]/.test(formData.password) ? 'valid' : ''}>
                      One uppercase letter (recommended)
                    </li>
                    <li className={/[0-9]/.test(formData.password) ? 'valid' : ''}>
                      One number (recommended)
                    </li>
                  </ul>
                </div>

                <div className="auth-form-actions">
                  <Button
                    type="button"
                    onClick={handleBack}
                    variant="secondary"
                    size="lg"
                    disabled={loading}
                  >
                    ← Back
                  </Button>

                  <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    loading={loading}
                    disabled={loading}
                    className="auth-submit-btn"
                  >
                    {loading ? 'Creating Account...' : 'Create Account'}
                  </Button>
                </div>
              </form>
            )}

            <div className="auth-divider">
              <span>or</span>
            </div>

            <div className="auth-links">
              <p>
                Already have an account?{' '}
                <Link to="/login" className="auth-link">
                  Sign in here
                </Link>
              </p>
            </div>
          </CardBody>
        </Card>
      </motion.div>
    </div>
  );
};

export default Register;
