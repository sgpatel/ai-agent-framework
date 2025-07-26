import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';

const UserAvatar = ({
  user,
  size = 32,
  className = '',
  showInitials = true,
  onClick = null,
  status = null, // online, offline, busy, away
  showStatus = false,
}) => {
  const [isHovered, setIsHovered] = useState(false);
  const [imageError, setImageError] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);

  // Reset image state when user changes
  useEffect(() => {
    setImageError(false);
    setImageLoaded(false);
  }, [user?.profilePictureUrl]);

  // Generate initials from user data
  const getInitials = (user) => {
    if (user?.firstName && user?.lastName) {
      return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
    }
    if (user?.username) {
      return user.username.slice(0, 2).toUpperCase();
    }
    if (user?.email) {
      return user.email.slice(0, 2).toUpperCase();
    }
    return 'AI';
  };

  // Generate consistent color based on user ID or username
  const getAvatarColor = (user) => {
    if (!user) return '#2563eb';

    const identifier = user.id || user.username || user.email || 'default';
    const hash = identifier.split('').reduce((a, b) => {
      a = ((a << 5) - a) + b.charCodeAt(0);
      return a & a;
    }, 0);

    const colors = [
      '#2563eb', '#7c3aed', '#059669', '#dc2626', '#ea580c',
      '#ca8a04', '#0891b2', '#c2410c', '#9333ea', '#16a34a',
      '#0369a1', '#be185d', '#a21caf', '#166534', '#92400e',
    ];

    return colors[Math.abs(hash) % colors.length];
  };

  // Get status color
  const getStatusColor = (status) => {
    switch (status) {
    case 'online': return '#10b981';
    case 'busy': return '#ef4444';
    case 'away': return '#f59e0b';
    case 'offline':
    default: return '#6b7280';
    }
  };

  // Handle image loading
  const handleImageLoad = () => {
    setImageLoaded(true);
    setImageError(false);
  };

  const handleImageError = () => {
    setImageError(true);
    setImageLoaded(false);
  };

  const hasValidImage = user?.profilePictureUrl && !imageError;

  const baseStyle = {
    width: `${size}px`,
    height: `${size}px`,
    borderRadius: '50%',
    background: hasValidImage
      ? 'transparent'
      : `linear-gradient(135deg, ${getAvatarColor(user)}, ${getAvatarColor(user)}dd)`,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: 'white',
    fontSize: `${Math.max(10, size * 0.4)}px`,
    fontWeight: '600',
    border: '2px solid rgba(255, 255, 255, 0.2)',
    cursor: onClick ? 'pointer' : 'default',
    transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
    textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)',
    position: 'relative',
    userSelect: 'none',
    overflow: 'hidden',
  };

  const hoverStyle = onClick && isHovered ? {
    transform: 'scale(1.05)',
    boxShadow: '0 8px 20px rgba(0, 0, 0, 0.15)',
    borderColor: 'rgba(255, 255, 255, 0.4)',
  } : {};

  const avatarStyle = { ...baseStyle, ...hoverStyle };

  const imageStyle = hasValidImage ? {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    borderRadius: '50%',
    display: imageLoaded ? 'block' : 'none',
  } : {};

  const statusIndicatorStyle = showStatus ? {
    position: 'absolute',
    bottom: '2px',
    right: '2px',
    width: `${Math.max(8, size * 0.25)}px`,
    height: `${Math.max(8, size * 0.25)}px`,
    backgroundColor: getStatusColor(status),
    border: '2px solid white',
    borderRadius: '50%',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.2)',
  } : null;

  return (
    <div
      className={`user-avatar ${className}`}
      style={avatarStyle}
      onClick={onClick}
      onMouseEnter={() => onClick && setIsHovered(true)}
      onMouseLeave={() => onClick && setIsHovered(false)}
      title={user?.fullName || user?.username || user?.email || 'User'}
      role={onClick ? 'button' : 'img'}
      tabIndex={onClick ? 0 : -1}
      onKeyDown={(e) => {
        if (onClick && (e.key === 'Enter' || e.key === ' ')) {
          e.preventDefault();
          onClick(e);
        }
      }}
      aria-label={`Avatar for ${user?.fullName || user?.username || 'User'}`}
    >
      {hasValidImage && (
        <img
          src={user.profilePictureUrl}
          alt={`${user?.fullName || user?.username || 'User'} avatar`}
          style={imageStyle}
          onLoad={handleImageLoad}
          onError={handleImageError}
          crossOrigin="anonymous"
        />
      )}
      {(!hasValidImage || !imageLoaded) && showInitials && getInitials(user)}
      {statusIndicatorStyle && <div style={statusIndicatorStyle} />}
    </div>
  );
};

UserAvatar.propTypes = {
  user: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    username: PropTypes.string,
    email: PropTypes.string,
    firstName: PropTypes.string,
    lastName: PropTypes.string,
    fullName: PropTypes.string,
    profilePictureUrl: PropTypes.string,
  }),
  size: PropTypes.number,
  className: PropTypes.string,
  showInitials: PropTypes.bool,
  onClick: PropTypes.func,
  status: PropTypes.oneOf(['online', 'offline', 'busy', 'away']),
  showStatus: PropTypes.bool,
};

export default UserAvatar;
