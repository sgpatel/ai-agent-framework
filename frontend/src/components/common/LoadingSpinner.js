import React from 'react';
import PropTypes from 'prop-types';
import { Spinner, Container, Row, Col } from 'react-bootstrap';
import './LoadingSpinner.css';

const LoadingSpinner = ({
  size = 'md',
  variant = 'primary',
  text = 'Loading...',
  centered = true,
  overlay = false,
  className = '',
  ...props
}) => {
  const sizeMap = {
    sm: { width: '1rem', height: '1rem' },
    md: { width: '2rem', height: '2rem' },
    lg: { width: '3rem', height: '3rem' },
    xl: { width: '4rem', height: '4rem' },
  };

  const spinnerElement = (
    <div className={`loading-spinner-wrapper ${className}`} {...props}>
      <Spinner
        animation='border'
        variant={variant}
        style={sizeMap[size]}
        role='status'
        aria-label={text}
      >
        <span className='visually-hidden'>{text}</span>
      </Spinner>
      {text && <div className='loading-text mt-2 text-muted'>{text}</div>}
    </div>
  );

  if (overlay) {
    return (
      <div className='loading-overlay'>
        <div className='loading-overlay-content'>{spinnerElement}</div>
      </div>
    );
  }

  if (centered) {
    return (
      <Container fluid className='loading-container'>
        <Row className='justify-content-center align-items-center min-vh-50'>
          <Col xs='auto'>{spinnerElement}</Col>
        </Row>
      </Container>
    );
  }

  return spinnerElement;
};

LoadingSpinner.propTypes = {
  size: PropTypes.oneOf(['sm', 'md', 'lg', 'xl']),
  variant: PropTypes.oneOf([
    'primary',
    'secondary',
    'success',
    'danger',
    'warning',
    'info',
    'light',
    'dark',
  ]),
  text: PropTypes.string,
  centered: PropTypes.bool,
  overlay: PropTypes.bool,
  className: PropTypes.string,
};

LoadingSpinner.defaultProps = {
  size: 'md',
  variant: 'primary',
  text: 'Loading...',
  centered: true,
  overlay: false,
  className: '',
};

export default LoadingSpinner;
