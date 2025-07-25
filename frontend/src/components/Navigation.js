import React from 'react';
import { Navbar, Nav, Container, NavDropdown } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

export default function Navigation() {
    return (
        <Navbar bg="dark" variant="dark" expand="lg" className="shadow-sm">
            <Container fluid>
                <Navbar.Brand className="fw-bold d-flex align-items-center">
                    <span className="me-2">🧠</span>
                    <span>AI Agent Framework</span>
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="nav" />
                <Navbar.Collapse id="nav">
                    <Nav className="me-auto">
                        {/* Dashboard - Main overview */}
                        <LinkContainer to="/dashboard">
                            <Nav.Link className="d-flex align-items-center">
                                <span className="me-2">📊</span>
                                Dashboard
                            </Nav.Link>
                        </LinkContainer>

                        {/* AI Agents - Scalable section for all agent types */}
                        <NavDropdown
                            title={
                                <span className="d-flex align-items-center">
                                    <span className="me-2">🤖</span>
                                    AI Agents
                                </span>
                            }
                            id="agents-dropdown"
                        >
                            <NavDropdown.Header>Financial Agents</NavDropdown.Header>
                            <LinkContainer to="/stocks">
                                <NavDropdown.Item>
                                    <span className="me-2">📈</span>
                                    Stock Analyzer
                                </NavDropdown.Item>
                            </LinkContainer>
                            <NavDropdown.Divider />

                            <NavDropdown.Header>Intelligence Agents</NavDropdown.Header>
                            <LinkContainer to="/intelligent">
                                <NavDropdown.Item>
                                    <span className="me-2">🧠</span>
                                    Smart Decisions
                                </NavDropdown.Item>
                            </LinkContainer>
                            <LinkContainer to="/ai-chat">
                                <NavDropdown.Item>
                                    <span className="me-2">💬</span>
                                    GPT4All Chat
                                </NavDropdown.Item>
                            </LinkContainer>
                            <NavDropdown.Divider />

                            <NavDropdown.Header>Future Agents</NavDropdown.Header>
                            <NavDropdown.Item disabled className="text-muted">
                                <span className="me-2">🔮</span>
                                More agents coming soon...
                            </NavDropdown.Item>
                        </NavDropdown>

                        {/* Analytics & Monitoring */}
                        <NavDropdown
                            title={
                                <span className="d-flex align-items-center">
                                    <span className="me-2">📊</span>
                                    Analytics
                                </span>
                            }
                            id="analytics-dropdown"
                        >
                            <LinkContainer to="/metrics">
                                <NavDropdown.Item>
                                    <span className="me-2">📊</span>
                                    System Metrics
                                </NavDropdown.Item>
                            </LinkContainer>
                            <LinkContainer to="/monitoring">
                                <NavDropdown.Item>
                                    <span className="me-2">🔍</span>
                                    System Monitor
                                </NavDropdown.Item>
                            </LinkContainer>
                            <NavDropdown.Divider />
                            <LinkContainer to="/context">
                                <NavDropdown.Item>
                                    <span className="me-2">🔄</span>
                                    Context Manager
                                </NavDropdown.Item>
                            </LinkContainer>
                        </NavDropdown>

                        {/* Task & Workflow Management */}
                        <NavDropdown
                            title={
                                <span className="d-flex align-items-center">
                                    <span className="me-2">⚙️</span>
                                    Management
                                </span>
                            }
                            id="management-dropdown"
                        >
                            <NavDropdown.Header>Task Management</NavDropdown.Header>
                            <LinkContainer to="/tasks/new">
                                <NavDropdown.Item>
                                    <span className="me-2">➕</span>
                                    Create Task
                                </NavDropdown.Item>
                            </LinkContainer>
                            <LinkContainer to="/tasks/history">
                                <NavDropdown.Item>
                                    <span className="me-2">📋</span>
                                    Task History
                                </NavDropdown.Item>
                            </LinkContainer>
                            <NavDropdown.Divider />

                            <NavDropdown.Header>System Configuration</NavDropdown.Header>
                            <LinkContainer to="/plugins">
                                <NavDropdown.Item>
                                    <span className="me-2">🔌</span>
                                    Plugins
                                </NavDropdown.Item>
                            </LinkContainer>
                        </NavDropdown>
                    </Nav>

                    {/* Right side navigation - User/Settings */}
                    <Nav>
                        <NavDropdown
                            title={
                                <span className="d-flex align-items-center">
                                    <span className="me-2">⚙️</span>
                                    Settings
                                </span>
                            }
                            id="settings-dropdown"
                            align="end"
                        >
                            <NavDropdown.Item>
                                <span className="me-2">🎨</span>
                                Theme
                            </NavDropdown.Item>
                            <NavDropdown.Item>
                                <span className="me-2">🔔</span>
                                Notifications
                            </NavDropdown.Item>
                            <NavDropdown.Divider />
                            <NavDropdown.Item>
                                <span className="me-2">❓</span>
                                Help & Support
                            </NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
