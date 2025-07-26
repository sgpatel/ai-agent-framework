import React from 'react';
import PropTypes from 'prop-types';

// Card Components
export const Card = ({ children, className = '', elevated = false, ...props }) => {
  const cardClass = `ai-card ${elevated ? 'ai-card--elevated' : ''} ${className}`;
  return (
    <div className={cardClass} {...props}>
      {children}
    </div>
  );
};

Card.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
  elevated: PropTypes.bool,
};

export const CardHeader = ({ children, className = '', ...props }) => (
  <div className={`ai-card__header ${className}`} {...props}>
    {children}
  </div>
);

CardHeader.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

export const CardTitle = ({ children, className = '', icon, ...props }) => (
  <h3 className={`ai-card__title ${className}`} {...props}>
    {icon && <span className="ai-card__icon">{icon}</span>}
    {children}
  </h3>
);

CardTitle.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
  icon: PropTypes.node,
};

export const CardSubtitle = ({ children, className = '', ...props }) => (
  <p className={`ai-card__subtitle ${className}`} {...props}>
    {children}
  </p>
);

CardSubtitle.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

export const CardBody = ({ children, className = '', ...props }) => (
  <div className={`ai-card__body ${className}`} {...props}>
    {children}
  </div>
);

CardBody.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

export const CardFooter = ({ children, className = '', ...props }) => (
  <div className={`ai-card__footer ${className}`} {...props}>
    {children}
  </div>
);

CardFooter.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

// Spinner Component (Fixed circular dependency)
export const Spinner = ({ size = 'md', className = '', ...props }) => {
  const spinnerClass = `ai-spinner ai-spinner--${size} ${className}`;
  return (
    <div className={spinnerClass} {...props}>
      <div className="ai-spinner__inner"></div>
    </div>
  );
};

Spinner.propTypes = {
  size: PropTypes.oneOf(['sm', 'md', 'lg']),
  className: PropTypes.string,
};

// Button Component
export const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  className = '',
  type = 'button',
  ...props
}) => {
  const buttonClass = `ai-btn ai-btn--${variant} ai-btn--${size} ${loading ? 'ai-btn--loading' : ''} ${className}`;

  return (
    <button
      type={type}
      className={buttonClass}
      disabled={disabled || loading}
      {...props}
    >
      {loading && <Spinner size="sm" />}
      <span className={loading ? 'ai-btn__content--loading' : 'ai-btn__content'}>
        {children}
      </span>
    </button>
  );
};

Button.propTypes = {
  children: PropTypes.node.isRequired,
  variant: PropTypes.oneOf(['primary', 'secondary', 'success', 'danger', 'warning', 'info', 'light', 'dark', 'outline-primary', 'outline-secondary']),
  size: PropTypes.oneOf(['sm', 'md', 'lg']),
  loading: PropTypes.bool,
  disabled: PropTypes.bool,
  className: PropTypes.string,
  type: PropTypes.oneOf(['button', 'submit', 'reset']),
};

// Input Component
export const Input = ({
  error = false,
  className = '',
  label,
  helperText,
  ...props
}) => {
  const inputClass = `ai-input ${error ? 'ai-input--error' : ''} ${className}`;

  return (
    <div className="ai-input-group">
      {label && <label className="ai-input-label">{label}</label>}
      <input className={inputClass} {...props} />
      {helperText && (
        <small className={`ai-input-helper ${error ? 'ai-input-helper--error' : ''}`}>
          {helperText}
        </small>
      )}
    </div>
  );
};

Input.propTypes = {
  error: PropTypes.bool,
  className: PropTypes.string,
  label: PropTypes.string,
  helperText: PropTypes.string,
};

// Badge Component
export const Badge = ({
  children,
  variant = 'primary',
  className = '',
  ...props
}) => {
  const badgeClass = `ai-badge ai-badge--${variant} ${className}`;

  return (
    <span className={badgeClass} {...props}>
      {children}
    </span>
  );
};

Badge.propTypes = {
  children: PropTypes.node.isRequired,
  variant: PropTypes.oneOf(['primary', 'secondary', 'success', 'danger', 'warning', 'info', 'light', 'dark']),
  className: PropTypes.string,
};

// Status Component
export const Status = ({
  status = 'offline',
  label,
  className = '',
  ...props
}) => {
  const statusClass = `ai-status ai-status--${status} ${className}`;

  return (
    <div className={statusClass} {...props}>
      <div className="ai-status__dot" />
      {label && <span className="ai-status__label">{label}</span>}
    </div>
  );
};

Status.propTypes = {
  status: PropTypes.oneOf(['online', 'offline', 'busy', 'away', 'idle']),
  label: PropTypes.string,
  className: PropTypes.string,
};

// Grid Component
export const Grid = ({
  children,
  cols = 1,
  gap = 'md',
  className = '',
  ...props
}) => {
  const gridClass = `ai-grid ai-grid--cols-${cols} ai-grid--gap-${gap} ${className}`;

  return (
    <div className={gridClass} {...props}>
      {children}
    </div>
  );
};

Grid.propTypes = {
  children: PropTypes.node.isRequired,
  cols: PropTypes.oneOf([1, 2, 3, 4, 5, 6, 12]),
  gap: PropTypes.oneOf(['none', 'sm', 'md', 'lg', 'xl']),
  className: PropTypes.string,
};

// Flex Component
export const Flex = ({
  children,
  direction = 'row',
  align = 'stretch',
  justify = 'start',
  gap = 'md',
  wrap = false,
  className = '',
  ...props
}) => {
  const flexClass = `ai-flex ai-flex--${direction} ai-flex--align-${align} ai-flex--justify-${justify} ai-flex--gap-${gap} ${wrap ? 'ai-flex--wrap' : ''} ${className}`;

  return (
    <div className={flexClass} {...props}>
      {children}
    </div>
  );
};

Flex.propTypes = {
  children: PropTypes.node.isRequired,
  direction: PropTypes.oneOf(['row', 'column', 'row-reverse', 'column-reverse']),
  align: PropTypes.oneOf(['start', 'end', 'center', 'stretch', 'baseline']),
  justify: PropTypes.oneOf(['start', 'end', 'center', 'between', 'around', 'evenly']),
  gap: PropTypes.oneOf(['none', 'sm', 'md', 'lg', 'xl']),
  wrap: PropTypes.bool,
  className: PropTypes.string,
};

// Alert Component
export const Alert = ({
  children,
  variant = 'info',
  dismissible = false,
  onDismiss,
  className = '',
  ...props
}) => {
  const alertClass = `ai-alert ai-alert--${variant} ${dismissible ? 'ai-alert--dismissible' : ''} ${className}`;

  return (
    <div className={alertClass} {...props}>
      <div className="ai-alert__content">{children}</div>
      {dismissible && (
        <button
          type="button"
          className="ai-alert__dismiss"
          onClick={onDismiss}
          aria-label="Dismiss alert"
        >
          ×
        </button>
      )}
    </div>
  );
};

Alert.propTypes = {
  children: PropTypes.node.isRequired,
  variant: PropTypes.oneOf(['primary', 'secondary', 'success', 'danger', 'warning', 'info', 'light', 'dark']),
  dismissible: PropTypes.bool,
  onDismiss: PropTypes.func,
  className: PropTypes.string,
};

// Loading Component
export const Loading = ({ message = 'Loading...', className = '' }) => (
  <div className={`ai-loading ${className}`}>
    <Spinner size="lg" />
    <p className="ai-loading__message">{message}</p>
  </div>
);

Loading.propTypes = {
  message: PropTypes.string,
  className: PropTypes.string,
};

// Skeleton Component
export const Skeleton = ({
  width = '100%',
  height = '1rem',
  className = '',
  variant = 'rectangular',
  animation = 'pulse',
  ...props
}) => {
  const skeletonClass = `ai-skeleton ai-skeleton--${variant} ai-skeleton--${animation} ${className}`;

  return (
    <div
      className={skeletonClass}
      style={{ width, height }}
      {...props}
    />
  );
};

Skeleton.propTypes = {
  width: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  height: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  className: PropTypes.string,
  variant: PropTypes.oneOf(['rectangular', 'circular', 'text']),
  animation: PropTypes.oneOf(['pulse', 'wave', 'none']),
};

// Container Component
export const Container = ({
  children,
  className = '',
  fluid = false,
  size = 'default',
  ...props
}) => {
  const containerClass = `ai-container ai-container--${size} ${
    fluid ? 'ai-container--fluid' : ''
  } ${className}`;

  return (
    <div className={containerClass} {...props}>
      {children}
    </div>
  );
};

Container.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
  fluid: PropTypes.bool,
  size: PropTypes.oneOf(['sm', 'md', 'lg', 'xl', 'default']),
};
