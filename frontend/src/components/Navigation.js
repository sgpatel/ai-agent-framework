import React, { useState, useEffect } from 'react';
import { Navbar, Nav, Container, Button, Dropdown } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { useLocation } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';
import { motion } from 'framer-motion';
import UserAvatar from './common/UserAvatar';
import './Navigation.css';
import '../styles/components.css';

export default function Navigation() {
  const location = useLocation();
  const { theme, toggleTheme, user } = useAppContext();
  const [expanded, setExpanded] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const isActive = path => location.pathname === path;

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleToggle = () => setExpanded(!expanded);
  const handleClose = () => setExpanded(false);

  const handleProfileClick = () => {
    setDropdownOpen(!dropdownOpen);
  };

  const handleLogout = () => {
    console.log('Logging out...');
    setDropdownOpen(false);
  };

  const navVariants = {
    hidden: { y: -100, opacity: 0 },
    visible: { 
      y: 0, 
      opacity: 1,
      transition: { duration: 0.6, ease: 'easeOut' },
    },
  };

  const brandVariants = {
    hover: { 
      scale: 1.05,
      transition: { duration: 0.2 },
    },
  };

  const linkVariants = {
    hover: { 
      y: -2,
      transition: { duration: 0.2 },
    },
  };

  return (
    <motion.div
      initial="hidden"
      animate="visible"
      variants={navVariants}
    >
      <Navbar 
        expand="lg" 
        fixed="top" 
        className={`navbar-professional ${isScrolled ? 'scrolled' : ''}`}
        expanded={expanded}
        onToggle={handleToggle}
      >
        <Container fluid className="px-4">
          {/* Brand */}
          <motion.div variants={brandVariants} whileHover="hover">
            <LinkContainer to="/">
              <Navbar.Brand className="navbar-brand-professional">
                <div className="brand-icon">
                  🤖
                </div>
                <span className="brand-text">AI Agent Framework</span>
              </Navbar.Brand>
            </LinkContainer>
          </motion.div>

          {/* Mobile menu toggle */}
          <Navbar.Toggle aria-controls="basic-navbar-nav" />

          {/* Navigation Items */}
          <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="navbar-nav-professional me-auto">
              <motion.div variants={linkVariants} whileHover="hover">
                <LinkContainer to="/">
                  <Nav.Link 
                    className={`nav-link-professional ${isActive('/') ? 'active' : ''}`}
                    onClick={handleClose}
                  >
                    🏠 Home
                  </Nav.Link>
                </LinkContainer>
              </motion.div>

              <motion.div variants={linkVariants} whileHover="hover">
                <LinkContainer to="/dashboard">
                  <Nav.Link
                    className={`nav-link-professional ${isActive('/dashboard') ? 'active' : ''}`}
                    onClick={handleClose}
                  >
                    🤖 Agents
                  </Nav.Link>
                </LinkContainer>
              </motion.div>

              <motion.div variants={linkVariants} whileHover="hover">
                <LinkContainer to="/context-manager">
                  <Nav.Link 
                    className={`nav-link-professional ${isActive('/context-manager') ? 'active' : ''}`}
                    onClick={handleClose}
                  >
                    🧠 Context Manager
                  </Nav.Link>
                </LinkContainer>
              </motion.div>

              <motion.div variants={linkVariants} whileHover="hover">
                <LinkContainer to="/intelligent-dashboard">
                  <Nav.Link 
                    className={`nav-link-professional ${isActive('/intelligent-dashboard') ? 'active' : ''}`}
                    onClick={handleClose}
                  >
                    📊 Analytics
                  </Nav.Link>
                </LinkContainer>
              </motion.div>

              <motion.div variants={linkVariants} whileHover="hover">
                <LinkContainer to="/stocks">
                  <Nav.Link
                    className={`nav-link-professional ${isActive('/stocks') ? 'active' : ''}`}
                    onClick={handleClose}
                  >
                    📈 Stocks
                  </Nav.Link>
                </LinkContainer>
              </motion.div>

              <motion.div variants={linkVariants} whileHover="hover">
                <LinkContainer to="/task-management">
                  <Nav.Link 
                    className={`nav-link-professional ${isActive('/task-management') ? 'active' : ''}`}
                    onClick={handleClose}
                  >
                    ✅ Tasks
                  </Nav.Link>
                </LinkContainer>
              </motion.div>

              <motion.div variants={linkVariants} whileHover="hover">
                <LinkContainer to="/plugin-manager">
                  <Nav.Link 
                    className={`nav-link-professional ${isActive('/plugin-manager') ? 'active' : ''}`}
                    onClick={handleClose}
                  >
                    🔌 Plugins
                  </Nav.Link>
                </LinkContainer>
              </motion.div>

              <motion.div variants={linkVariants} whileHover="hover">
                <LinkContainer to="/ai-chat">
                  <Nav.Link
                    className={`nav-link-professional ${isActive('/ai-chat') ? 'active' : ''}`}
                    onClick={handleClose}
                  >
                    💬 AI Chat
                  </Nav.Link>
                </LinkContainer>
              </motion.div>
            </Nav>

            {/* Right side items */}
            <Nav className="navbar-nav-professional align-items-lg-center">
              {/* Search */}
              <div className="search-container-professional me-3">
                <input
                  type="search"
                  placeholder="Search..."
                  className="search-input-professional"
                />
              </div>

              {/* Notifications */}
              <Button 
                variant="outline-secondary" 
                className="notification-bell me-2 border-0"
                aria-label="Notifications"
              >
                🔔
                <span className="notification-badge">3</span>
              </Button>

              {/* Theme Toggle */}
              <div 
                className="theme-toggle me-3"
                onClick={toggleTheme}
                role="button"
                tabIndex={0}
                aria-label={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    toggleTheme();
                  }
                }}
              >
                <div className="theme-toggle-slider">
                  {theme === 'light' ? '🌙' : '☀️'}
                </div>
              </div>

              {/* User Menu */}
              <Dropdown show={dropdownOpen} onToggle={setDropdownOpen}>
                <Dropdown.Toggle 
                  as="button"
                  className="dropdown-toggle-professional"
                  onClick={handleProfileClick}
                  aria-label="User menu"
                >
                  <div className="user-avatar-professional">
                    {user?.name ? user.name.charAt(0).toUpperCase() : '👤'}
                  </div>
                  <span className="d-none d-lg-inline">{user?.name || 'Guest'}</span>
                  <span>▼</span>
                </Dropdown.Toggle>

                <Dropdown.Menu className="dropdown-menu-professional">
                  <LinkContainer to="/profile">
                    <Dropdown.Item className="dropdown-item-professional" onClick={handleClose}>
                      👤 Profile
                    </Dropdown.Item>
                  </LinkContainer>
                  
                  <LinkContainer to="/settings">
                    <Dropdown.Item className="dropdown-item-professional" onClick={handleClose}>
                      ⚙️ Settings
                    </Dropdown.Item>
                  </LinkContainer>
                  
                  <Dropdown.Divider className="dropdown-divider-professional" />
                  
                  <Dropdown.Item 
                    className="dropdown-item-professional" 
                    onClick={handleLogout}
                  >
                    🚪 Logout
                  </Dropdown.Item>
                </Dropdown.Menu>
              </Dropdown>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
    </motion.div>
  );
}
