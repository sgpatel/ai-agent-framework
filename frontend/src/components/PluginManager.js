import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Button,
  Modal,
  Form,
  Alert,
  Badge,
  Tab,
  Nav,
  Spinner,
  ProgressBar,
  Table,
  Dropdown,
  InputGroup,
  Toast,
  ToastContainer,
} from 'react-bootstrap';
import { useAppContext } from '../context/AppContext';
import { pluginApi } from '../services/api';

export default function PluginManager() {
  const { updateSharedData, setAgentContext } = useAppContext();

  // Core state
  const [installedPlugins, setInstalledPlugins] = useState([]);
  const [availablePlugins, setAvailablePlugins] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('installed');

  // Modal states
  const [showInstallModal, setShowInstallModal] = useState(false);
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [showDependencyModal, setShowDependencyModal] = useState(false);

  // Plugin management states
  const [selectedPlugin, setSelectedPlugin] = useState(null);
  const [installProgress, setInstallProgress] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterCategory, setFilterCategory] = useState('all');
  const [sortBy, setSortBy] = useState('name');

  // Installation states
  const [uploadFile, setUploadFile] = useState(null);
  const [customPluginUrl, setCustomPluginUrl] = useState('');
  const [pluginConfig, setPluginConfig] = useState({});

  // Toast notifications
  const [toasts, setToasts] = useState([]);

  // Load real plugin data from API
  const loadPluginData = async () => {
    setLoading(true);
    try {
      const [installedRes, availableRes] = await Promise.all([
        pluginApi.getInstalledPlugins(),
        pluginApi.getAvailablePlugins(),
      ]);

      // Backend returns data directly, not wrapped in .data property
      setInstalledPlugins(installedRes.data || installedRes || []);
      setAvailablePlugins(availableRes.data || availableRes || []);
    } catch (err) {
      setError('Failed to load plugin data: ' + err.message);
      // Fallback to mock data for demonstration
      loadMockData();
    } finally {
      setLoading(false);
    }
  };

  const loadMockData = () => {
    // Keep existing mock data as fallback
    setInstalledPlugins([
      {
        id: 'stock-analyzer-pro',
        name: 'Stock Analyzer Pro',
        version: '2.1.4',
        category: 'Financial',
        description: 'Advanced stock analysis with ML predictions',
        author: 'FinTech Solutions',
        status: 'active',
        size: '12.5 MB',
        lastUpdated: '2025-07-20',
        dependencies: ['numpy', 'pandas', 'tensorflow'],
        permissions: ['market-data', 'file-system'],
        rating: 4.8,
        downloads: 15420,
        hasUpdate: true,
        newVersion: '2.2.0',
      },
      {
        id: 'risk-assessor',
        name: 'AI Risk Assessor',
        version: '1.3.2',
        category: 'Analytics',
        description: 'Intelligent risk assessment and portfolio optimization',
        author: 'Risk Analytics Inc',
        status: 'active',
        size: '8.2 MB',
        lastUpdated: '2025-07-18',
        dependencies: ['scipy', 'matplotlib'],
        permissions: ['portfolio-data'],
        rating: 4.6,
        downloads: 8934,
        hasUpdate: false,
      },
      {
        id: 'chart-visualizer',
        name: 'Advanced Chart Visualizer',
        version: '3.0.1',
        category: 'Visualization',
        description: 'Interactive charts and technical indicators',
        author: 'DataViz Pro',
        status: 'inactive',
        size: '15.8 MB',
        lastUpdated: '2025-07-15',
        dependencies: ['d3', 'plotly', 'chart.js'],
        permissions: ['display'],
        rating: 4.9,
        downloads: 23145,
        hasUpdate: false,
      },
    ]);

    setAvailablePlugins([
      {
        id: 'sentiment-analyzer',
        name: 'Market Sentiment Analyzer',
        version: '1.5.0',
        category: 'AI/ML',
        description: 'Real-time market sentiment analysis using NLP',
        author: 'SentimentAI',
        size: '25.3 MB',
        rating: 4.7,
        downloads: 12847,
        price: 'Free',
        compatibility: '✓ Compatible',
        features: ['Real-time analysis', 'Multiple sources', 'API integration'],
      },
      {
        id: 'crypto-tracker',
        name: 'Cryptocurrency Tracker',
        version: '2.0.8',
        category: 'Financial',
        description: 'Comprehensive cryptocurrency portfolio tracking',
        author: 'CryptoTools',
        size: '18.7 MB',
        rating: 4.5,
        downloads: 9876,
        price: '$29.99',
        compatibility: '✓ Compatible',
        features: ['Multi-exchange support', 'DeFi tracking', 'Tax reporting'],
      },
      {
        id: 'news-aggregator',
        name: 'Financial News Aggregator',
        version: '1.2.3',
        category: 'Data Sources',
        description: 'Aggregate and analyze financial news from multiple sources',
        author: 'NewsFlow',
        size: '7.4 MB',
        rating: 4.3,
        downloads: 5432,
        price: 'Free',
        compatibility: '⚠ Requires update',
        features: ['RSS feeds', 'Keyword filtering', 'Sentiment scoring'],
      },
    ]);
  };

  useEffect(() => {
    loadPluginData();
  }, []);

  const showToast = (message, type = 'success') => {
    const toast = {
      id: Date.now(),
      message,
      type,
      show: true,
    };
    setToasts(prev => [...prev, toast]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== toast.id));
    }, 5000);
  };

  const handlePluginAction = async (plugin, action) => {
    setLoading(true);
    try {
      let response;
      switch (action) {
      case 'activate':
      case 'deactivate':
        response = await pluginApi.togglePlugin(plugin.id);
        setInstalledPlugins(prev =>
          prev.map(p => (p.id === plugin.id ? { ...p, status: response.data.newStatus } : p)),
        );
        showToast(`${plugin.name} ${response.data.newStatus}`);
        break;

      case 'uninstall':
        response = await pluginApi.uninstallPlugin(plugin.id);
        setInstalledPlugins(prev => prev.filter(p => p.id !== plugin.id));
        showToast(`${plugin.name} uninstalled`, 'warning');
        break;

      case 'update':
        response = await pluginApi.updatePlugin(plugin.id);
        setInstalledPlugins(prev =>
          prev.map(p =>
            p.id === plugin.id
              ? {
                ...p,
                version: response.data.newVersion,
                hasUpdate: false,
                lastUpdated: new Date().toISOString().split('T')[0],
              }
              : p,
          ),
        );
        showToast(`${plugin.name} updated to version ${response.data.newVersion}`);
        break;
      }
    } catch (err) {
      setError(`Failed to ${action} plugin: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleInstallPlugin = async plugin => {
    setSelectedPlugin(plugin);
    setShowInstallModal(true);
  };

  const confirmInstall = async () => {
    if (!selectedPlugin) return;

    setShowInstallModal(false);
    setLoading(true);
    setInstallProgress(0);

    try {
      // Show installation progress
      const progressInterval = setInterval(() => {
        setInstallProgress(prev => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return 90;
          }
          return prev + 10;
        });
      }, 200);

      // Install plugin via API
      const response = await pluginApi.installPlugin(selectedPlugin.id);

      clearInterval(progressInterval);
      setInstallProgress(100);

      // Update state
      setInstalledPlugins(prev => [...prev, response.data.plugin]);
      setAvailablePlugins(prev => prev.filter(p => p.id !== selectedPlugin.id));

      showToast(`${selectedPlugin.name} installed successfully`);

      // Update context for agent collaboration
      setAgentContext('plugin-manager', {
        dataType: 'plugin-installation',
        installedPlugin: selectedPlugin.id,
        category: selectedPlugin.category,
      });
    } catch (err) {
      setError(`Installation failed: ${err.message}`);
    } finally {
      setLoading(false);
      setInstallProgress(0);
      setSelectedPlugin(null);
    }
  };

  const handleConfigurePlugin = plugin => {
    setSelectedPlugin(plugin);
    setPluginConfig(plugin.config || {});
    setShowConfigModal(true);
  };

  const savePluginConfig = async () => {
    if (!selectedPlugin) return;

    try {
      await pluginApi.configurePlugin(selectedPlugin.id, pluginConfig);

      setInstalledPlugins(prev =>
        prev.map(p => (p.id === selectedPlugin.id ? { ...p, config: pluginConfig } : p)),
      );

      showToast(`Configuration saved for ${selectedPlugin.name}`);
      setShowConfigModal(false);
      setSelectedPlugin(null);
      setPluginConfig({});
    } catch (err) {
      setError(`Failed to save configuration: ${err.message}`);
    }
  };

  const handleUploadPlugin = async () => {
    if (!uploadFile && !customPluginUrl) return;

    try {
      const formData = new FormData();
      if (uploadFile) {
        formData.append('file', uploadFile);
      }
      if (customPluginUrl) {
        formData.append('url', customPluginUrl);
      }

      const response = await pluginApi.uploadPlugin(formData);

      showToast('Custom plugin uploaded successfully');
      setShowUploadModal(false);
      setUploadFile(null);
      setCustomPluginUrl('');

      // Reload plugin data
      loadPluginData();
    } catch (err) {
      setError(`Upload failed: ${err.message}`);
    }
  };

  const filteredInstalledPlugins = installedPlugins
    .filter(
      plugin =>
        plugin.name.toLowerCase().includes(searchTerm.toLowerCase()) &&
        (filterCategory === 'all' || plugin.category === filterCategory),
    )
    .sort((a, b) => {
      switch (sortBy) {
      case 'name':
        return a.name.localeCompare(b.name);
      case 'date':
        return new Date(b.lastUpdated) - new Date(a.lastUpdated);
      case 'rating':
        return b.rating - a.rating;
      default:
        return 0;
      }
    });

  const filteredAvailablePlugins = availablePlugins.filter(
    plugin =>
      plugin.name.toLowerCase().includes(searchTerm.toLowerCase()) &&
      (filterCategory === 'all' || plugin.category === filterCategory),
  );

  const categories = ['all', 'Financial', 'Analytics', 'Visualization', 'AI/ML', 'Data Sources'];

  return (
    <div className='plugin-manager p-4'>
      {/* Header */}
      <Row className='mb-4'>
        <Col>
          <h2>🔌 Advanced Plugin Manager</h2>
          <p className='text-muted'>
            Manage and extend your AI Agent Framework with powerful plugins
          </p>
        </Col>
        <Col xs='auto'>
          <Button variant='primary' onClick={() => setShowUploadModal(true)} className='me-2'>
            📁 Upload Plugin
          </Button>
          <Button variant='outline-primary' onClick={loadPluginData}>
            🔄 Refresh
          </Button>
        </Col>
      </Row>

      {/* Error Alert */}
      {error && (
        <Alert variant='danger' dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Search and Filters */}
      <Card className='mb-4'>
        <Card.Body>
          <Row>
            <Col md={4}>
              <InputGroup>
                <InputGroup.Text>🔍</InputGroup.Text>
                <Form.Control
                  type='text'
                  placeholder='Search plugins...'
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </Col>
            <Col md={3}>
              <Form.Select value={filterCategory} onChange={e => setFilterCategory(e.target.value)}>
                {categories.map(cat => (
                  <option key={cat} value={cat}>
                    {cat === 'all' ? 'All Categories' : cat}
                  </option>
                ))}
              </Form.Select>
            </Col>
            <Col md={3}>
              <Form.Select value={sortBy} onChange={e => setSortBy(e.target.value)}>
                <option value='name'>Sort by Name</option>
                <option value='date'>Sort by Date</option>
                <option value='rating'>Sort by Rating</option>
              </Form.Select>
            </Col>
            <Col md={2}>
              <Badge bg='info' className='w-100 p-2'>
                {filteredInstalledPlugins.length} installed
              </Badge>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Installation Progress */}
      {installProgress > 0 && (
        <Card className='mb-4'>
          <Card.Body>
            <h6>Installing {selectedPlugin?.name}...</h6>
            <ProgressBar now={installProgress} label={`${installProgress}%`} />
          </Card.Body>
        </Card>
      )}

      {/* Main Content Tabs */}
      <Tab.Container activeKey={activeTab} onSelect={setActiveTab}>
        <Nav variant='pills' className='mb-4'>
          <Nav.Item>
            <Nav.Link eventKey='installed'>
              📦 Installed Plugins ({installedPlugins.length})
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey='available'>
              🌐 Available Plugins ({availablePlugins.length})
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey='dependencies'>🔗 Dependencies</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey='marketplace'>🏪 Marketplace</Nav.Link>
          </Nav.Item>
        </Nav>

        <Tab.Content>
          {/* Installed Plugins Tab */}
          <Tab.Pane eventKey='installed'>
            {loading ? (
              <div className='text-center p-4'>
                <Spinner animation='border' />
                <p className='mt-2'>Loading plugins...</p>
              </div>
            ) : (
              <Row>
                {filteredInstalledPlugins.map(plugin => (
                  <Col lg={6} xl={4} key={plugin.id} className='mb-4'>
                    <Card
                      className={`h-100 ${plugin.status === 'active' ? 'border-success' : 'border-secondary'}`}
                    >
                      <Card.Header className='d-flex justify-content-between align-items-center'>
                        <div>
                          <h6 className='mb-0'>{plugin.name}</h6>
                          <small className='text-muted'>v{plugin.version}</small>
                        </div>
                        <div>
                          <Badge bg={plugin.status === 'active' ? 'success' : 'secondary'}>
                            {plugin.status}
                          </Badge>
                          {plugin.hasUpdate && (
                            <Badge bg='warning' className='ms-1'>
                              Update Available
                            </Badge>
                          )}
                        </div>
                      </Card.Header>
                      <Card.Body>
                        <p className='small text-muted'>{plugin.description}</p>
                        <div className='mb-2'>
                          <Badge bg='outline-primary' className='me-1'>
                            {plugin.category}
                          </Badge>
                          <Badge bg='outline-secondary'>{plugin.size}</Badge>
                        </div>
                        <div className='small text-muted mb-3'>
                          <div>
                            ⭐ {plugin.rating} • 📥 {plugin.downloads.toLocaleString()}
                          </div>
                          <div>👤 {plugin.author}</div>
                          <div>📅 Updated: {plugin.lastUpdated}</div>
                        </div>
                        <div className='d-flex gap-1 flex-wrap'>
                          {plugin.status === 'active' ? (
                            <Button
                              size='sm'
                              variant='outline-warning'
                              onClick={() => handlePluginAction(plugin, 'deactivate')}
                            >
                              ⏸️ Deactivate
                            </Button>
                          ) : (
                            <Button
                              size='sm'
                              variant='outline-success'
                              onClick={() => handlePluginAction(plugin, 'activate')}
                            >
                              ▶️ Activate
                            </Button>
                          )}
                          <Button
                            size='sm'
                            variant='outline-primary'
                            onClick={() => handleConfigurePlugin(plugin)}
                          >
                            ⚙️ Configure
                          </Button>
                          {plugin.hasUpdate && (
                            <Button
                              size='sm'
                              variant='outline-info'
                              onClick={() => handlePluginAction(plugin, 'update')}
                            >
                              🔄 Update
                            </Button>
                          )}
                          <Button
                            size='sm'
                            variant='outline-danger'
                            onClick={() => handlePluginAction(plugin, 'uninstall')}
                          >
                            🗑️ Uninstall
                          </Button>
                        </div>
                      </Card.Body>
                    </Card>
                  </Col>
                ))}
              </Row>
            )}
          </Tab.Pane>

          {/* Available Plugins Tab */}
          <Tab.Pane eventKey='available'>
            <Row>
              {filteredAvailablePlugins.map(plugin => (
                <Col lg={6} xl={4} key={plugin.id} className='mb-4'>
                  <Card className='h-100'>
                    <Card.Header>
                      <div className='d-flex justify-content-between align-items-center'>
                        <div>
                          <h6 className='mb-0'>{plugin.name}</h6>
                          <small className='text-muted'>v{plugin.version}</small>
                        </div>
                        <Badge bg={plugin.price === 'Free' ? 'success' : 'primary'}>
                          {plugin.price}
                        </Badge>
                      </div>
                    </Card.Header>
                    <Card.Body>
                      <p className='small text-muted'>{plugin.description}</p>
                      <div className='mb-2'>
                        <Badge bg='outline-primary' className='me-1'>
                          {plugin.category}
                        </Badge>
                        <Badge bg='outline-secondary'>{plugin.size}</Badge>
                      </div>
                      <div className='small text-muted mb-2'>
                        <div>
                          ⭐ {plugin.rating} • 📥 {plugin.downloads.toLocaleString()}
                        </div>
                        <div>👤 {plugin.author}</div>
                        <div
                          className={
                            plugin.compatibility.includes('Compatible')
                              ? 'text-success'
                              : 'text-warning'
                          }
                        >
                          {plugin.compatibility}
                        </div>
                      </div>
                      <div className='mb-3'>
                        <strong>Features:</strong>
                        <ul className='small mb-0'>
                          {plugin.features.map((feature, idx) => (
                            <li key={idx}>{feature}</li>
                          ))}
                        </ul>
                      </div>
                      <Button
                        variant='primary'
                        size='sm'
                        className='w-100'
                        onClick={() => handleInstallPlugin(plugin)}
                        disabled={!plugin.compatibility.includes('Compatible')}
                      >
                        📥 Install Plugin
                      </Button>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          </Tab.Pane>

          {/* Dependencies Tab */}
          <Tab.Pane eventKey='dependencies'>
            <Card>
              <Card.Header>
                <h5>🔗 Plugin Dependencies</h5>
              </Card.Header>
              <Card.Body>
                <Table responsive>
                  <thead>
                    <tr>
                      <th>Plugin</th>
                      <th>Dependencies</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {installedPlugins.map(plugin => (
                      <tr key={plugin.id}>
                        <td>
                          <strong>{plugin.name}</strong>
                          <br />
                          <small className='text-muted'>v{plugin.version}</small>
                        </td>
                        <td>
                          {plugin.dependencies.map(dep => (
                            <Badge key={dep} bg='outline-secondary' className='me-1 mb-1'>
                              {dep}
                            </Badge>
                          ))}
                        </td>
                        <td>
                          <Badge bg='success'>✓ Satisfied</Badge>
                        </td>
                        <td>
                          <Button size='sm' variant='outline-primary'>
                            🔍 Check
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Tab.Pane>

          {/* Marketplace Tab */}
          <Tab.Pane eventKey='marketplace'>
            <Row>
              <Col md={8}>
                <Card>
                  <Card.Header>
                    <h5>🏪 Plugin Marketplace</h5>
                  </Card.Header>
                  <Card.Body>
                    <Alert variant='info'>
                      <h6>🎯 Featured Plugins</h6>
                      <p className='mb-0'>
                        Discover trending plugins that integrate seamlessly with your AI Agent
                        Framework.
                      </p>
                    </Alert>

                    <div className='mb-3'>
                      <h6>🔥 Trending This Week</h6>
                      <div className='d-flex gap-2 flex-wrap'>
                        <Badge bg='primary'>Sentiment Analyzer</Badge>
                        <Badge bg='success'>Crypto Tracker</Badge>
                        <Badge bg='info'>News Aggregator</Badge>
                      </div>
                    </div>

                    <div className='mb-3'>
                      <h6>📊 Popular Categories</h6>
                      <Row>
                        <Col sm={6}>
                          <Card className='mb-2'>
                            <Card.Body className='p-3'>
                              <h6>💰 Financial Tools</h6>
                              <small>25 plugins available</small>
                            </Card.Body>
                          </Card>
                        </Col>
                        <Col sm={6}>
                          <Card className='mb-2'>
                            <Card.Body className='p-3'>
                              <h6>🤖 AI/ML Extensions</h6>
                              <small>18 plugins available</small>
                            </Card.Body>
                          </Card>
                        </Col>
                      </Row>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card>
                  <Card.Header>
                    <h6>🔗 Quick Links</h6>
                  </Card.Header>
                  <Card.Body>
                    <div className='d-grid gap-2'>
                      <Button variant='outline-primary' size='sm'>
                        📚 Plugin Documentation
                      </Button>
                      <Button variant='outline-success' size='sm'>
                        👨‍💻 Developer SDK
                      </Button>
                      <Button variant='outline-info' size='sm'>
                        💬 Community Forum
                      </Button>
                      <Button variant='outline-warning' size='sm'>
                        🐛 Report Issues
                      </Button>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          </Tab.Pane>
        </Tab.Content>
      </Tab.Container>

      {/* Install Confirmation Modal */}
      <Modal show={showInstallModal} onHide={() => setShowInstallModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Install Plugin</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedPlugin && (
            <>
              <h6>{selectedPlugin.name}</h6>
              <p>{selectedPlugin.description}</p>
              <div className='mb-3'>
                <strong>Version:</strong> {selectedPlugin.version}
                <br />
                <strong>Size:</strong> {selectedPlugin.size}
                <br />
                <strong>Price:</strong> {selectedPlugin.price}
              </div>
              <Alert variant='info'>This plugin will be installed and activated immediately.</Alert>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant='secondary' onClick={() => setShowInstallModal(false)}>
            Cancel
          </Button>
          <Button variant='primary' onClick={confirmInstall}>
            Install Plugin
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Configuration Modal */}
      <Modal show={showConfigModal} onHide={() => setShowConfigModal(false)} size='lg'>
        <Modal.Header closeButton>
          <Modal.Title>Configure {selectedPlugin?.name}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className='mb-3'>
              <Form.Label>API Endpoint</Form.Label>
              <Form.Control
                type='text'
                placeholder='https://api.example.com'
                value={pluginConfig.apiEndpoint || ''}
                onChange={e => setPluginConfig({ ...pluginConfig, apiEndpoint: e.target.value })}
              />
            </Form.Group>
            <Form.Group className='mb-3'>
              <Form.Label>API Key</Form.Label>
              <Form.Control
                type='password'
                placeholder='Enter API key'
                value={pluginConfig.apiKey || ''}
                onChange={e => setPluginConfig({ ...pluginConfig, apiKey: e.target.value })}
              />
            </Form.Group>
            <Form.Group className='mb-3'>
              <Form.Check
                type='checkbox'
                label='Enable debug logging'
                checked={pluginConfig.debugLogging || false}
                onChange={e => setPluginConfig({ ...pluginConfig, debugLogging: e.target.checked })}
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant='secondary' onClick={() => setShowConfigModal(false)}>
            Cancel
          </Button>
          <Button variant='primary' onClick={savePluginConfig}>
            Save Configuration
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Upload Plugin Modal */}
      <Modal show={showUploadModal} onHide={() => setShowUploadModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Upload Custom Plugin</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className='mb-3'>
              <Form.Label>Plugin File (.jar, .zip)</Form.Label>
              <Form.Control
                type='file'
                accept='.jar,.zip'
                onChange={e => setUploadFile(e.target.files[0])}
              />
            </Form.Group>
            <div className='text-center my-3'>
              <strong>OR</strong>
            </div>
            <Form.Group className='mb-3'>
              <Form.Label>Plugin URL</Form.Label>
              <Form.Control
                type='url'
                placeholder='https://example.com/plugin.jar'
                value={customPluginUrl}
                onChange={e => setCustomPluginUrl(e.target.value)}
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant='secondary' onClick={() => setShowUploadModal(false)}>
            Cancel
          </Button>
          <Button
            variant='primary'
            disabled={!uploadFile && !customPluginUrl}
            onClick={handleUploadPlugin}
          >
            Upload Plugin
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Toast Notifications */}
      <ToastContainer position='top-end' className='p-3'>
        {toasts.map(toast => (
          <Toast key={toast.id} show={toast.show} bg={toast.type}>
            <Toast.Body className='text-white'>{toast.message}</Toast.Body>
          </Toast>
        ))}
      </ToastContainer>
    </div>
  );
}
