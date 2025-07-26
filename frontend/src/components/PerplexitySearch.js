import React, { useState, useEffect, useCallback } from 'react';
import './PerplexitySearch.css';

const PerplexitySearch = () => {
  const [query, setQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null);
  const [mapVisualization, setMapVisualization] = useState(null);
  const [loading, setLoading] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [searchType, setSearchType] = useState('comprehensive');

  // Debounced search suggestions
  useEffect(() => {
    const timer = setTimeout(() => {
      if (query.length > 2) {
        fetchSuggestions(query);
      } else {
        setSuggestions([]);
        setShowSuggestions(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  const fetchSuggestions = async searchQuery => {
    try {
      const response = await fetch(
        `/api/search/suggestions?query=${encodeURIComponent(searchQuery)}`,
      );
      const data = await response.json();
      setSuggestions(data.suggestions || []);
      setShowSuggestions(true);
    } catch (error) {
      console.error('Failed to fetch suggestions:', error);
    }
  };

  const performSearch = async (searchQuery = query) => {
    if (!searchQuery.trim()) return;

    setLoading(true);
    setShowSuggestions(false);

    try {
      const response = await fetch('/api/search/intelligent', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          query: searchQuery,
          searchType: searchType,
        }),
      });

      const data = await response.json();

      if (response.ok) {
        setSearchResults(data.searchResults);
        setMapVisualization(data.mapVisualization);
      } else {
        console.error('Search failed:', data.error);
        setSearchResults({ error: data.error });
      }
    } catch (error) {
      console.error('Search error:', error);
      setSearchResults({ error: 'Failed to perform search' });
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = e => {
    e.preventDefault();
    performSearch();
  };

  const handleSuggestionClick = suggestion => {
    setQuery(suggestion);
    setShowSuggestions(false);
    performSearch(suggestion);
  };

  const renderSearchResults = () => {
    if (!searchResults) return null;

    if (searchResults.error) {
      return (
        <div className='search-error'>
          <h3>Search Error</h3>
          <p>{searchResults.error}</p>
        </div>
      );
    }

    const synthesis = searchResults.synthesis;
    const webResults = searchResults.webResults;
    const newsResults = searchResults.newsResults;
    const locationResults = searchResults.locationResults;
    const sources = searchResults.sources;

    return (
      <div className='search-results'>
        {/* Main Answer */}
        {synthesis && (
          <div className='main-answer'>
            <h2>Answer</h2>
            <div className='answer-content'>
              <pre>{synthesis.comprehensiveAnswer}</pre>
            </div>
            <div className='answer-metadata'>
              <span className='confidence'>
                Confidence: {Math.round((synthesis.confidence || 0) * 100)}%
              </span>
              {synthesis.hasLocationContext && (
                <span className='location-indicator'>📍 Location context available</span>
              )}
            </div>
          </div>
        )}

        {/* Map Visualization */}
        {mapVisualization && renderMapVisualization()}

        {/* Web Results */}
        {webResults && webResults.length > 0 && (
          <div className='web-results'>
            <h3>Web Sources</h3>
            {webResults.slice(0, 5).map((result, index) => (
              <div key={index} className='web-result'>
                <h4>{result.title}</h4>
                <p>{result.snippet}</p>
                <div className='result-metadata'>
                  <a href={result.url} target='_blank' rel='noopener noreferrer'>
                    {result.source}
                  </a>
                  <span className='relevance'>
                    Relevance: {Math.round((result.relevanceScore || 0) * 100)}%
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* News Results */}
        {newsResults && newsResults.length > 0 && (
          <div className='news-results'>
            <h3>Recent News</h3>
            {newsResults.map((news, index) => (
              <div key={index} className='news-result'>
                <h4>{news.headline}</h4>
                <p>{news.summary}</p>
                <div className='news-metadata'>
                  <span className='source'>{news.source}</span>
                  <span className='date'>{new Date(news.publishedAt).toLocaleDateString()}</span>
                  <span className='credibility'>
                    Credibility: {Math.round((news.credibilityScore || 0) * 100)}%
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Location Results */}
        {locationResults && Object.keys(locationResults).length > 0 && (
          <div className='location-results'>
            <h3>Location Information</h3>
            {Object.entries(locationResults).map(([location, data]) => (
              <div key={location} className='location-result'>
                <h4>{data.name}</h4>
                <p>{data.description}</p>
                <div className='location-metadata'>
                  <span className='type'>{data.type}</span>
                  <span className='country'>{data.country}</span>
                  {data.population && (
                    <span className='population'>
                      Population: {data.population.toLocaleString()}
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Sources */}
        {sources && sources.length > 0 && (
          <div className='sources'>
            <h3>Sources</h3>
            <div className='sources-list'>
              {sources.map((source, index) => (
                <div key={index} className='source-item'>
                  <a href={source.url} target='_blank' rel='noopener noreferrer'>
                    {source.title}
                  </a>
                  <span className='source-type'>{source.type}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderMapVisualization = () => {
    if (!mapVisualization) return null;

    const mapConfig = mapVisualization.mapConfig;
    const markers = mapVisualization.markers;
    const metadata = mapVisualization.metadata;

    return (
      <div className='map-visualization'>
        <h3>Map Visualization</h3>
        <div className='map-container'>
          <div className='map-placeholder'>
            <div className='map-info'>
              <h4>Interactive Map</h4>
              <p>Query: {mapVisualization.query}</p>
              <p>Map Type: {mapConfig?.type || 'roadmap'}</p>
              <p>Zoom Level: {mapConfig?.zoom || 10}</p>
              {markers && markers.length > 0 && <p>Locations: {markers.length} markers</p>}
            </div>

            {/* Mock map display */}
            <div className='mock-map'>
              <div className='map-center'>
                {mapConfig?.center && (
                  <div className='coordinates'>
                    Center: {mapConfig.center.lat.toFixed(4)}, {mapConfig.center.lng.toFixed(4)}
                  </div>
                )}
              </div>

              {markers &&
                markers.map((marker, index) => (
                  <div key={index} className='map-marker'>
                    <div className='marker-icon'>📍</div>
                    <div className='marker-info'>
                      <strong>{marker.title}</strong>
                      <p>{marker.description}</p>
                    </div>
                  </div>
                ))}
            </div>
          </div>

          {metadata && (
            <div className='map-metadata'>
              <span>Generated: {new Date(metadata.generatedAt).toLocaleString()}</span>
              <span>Provider: {metadata.mapProvider}</span>
              <span>Confidence: {Math.round((metadata.confidence || 0) * 100)}%</span>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className='perplexity-search'>
      <div className='search-header'>
        <h1>Intelligent Search</h1>
        <p>Ask anything and get comprehensive answers with automatic map integration</p>
      </div>

      <form onSubmit={handleSubmit} className='search-form'>
        <div className='search-input-container'>
          <input
            type='text'
            value={query}
            onChange={e => setQuery(e.target.value)}
            placeholder="Ask anything... (e.g., 'best restaurants in Paris', 'latest AI news')"
            className='search-input'
            autoComplete='off'
          />

          <div className='search-controls'>
            <select
              value={searchType}
              onChange={e => setSearchType(e.target.value)}
              className='search-type-select'
            >
              <option value='comprehensive'>Comprehensive</option>
              <option value='news'>News Focus</option>
              <option value='academic'>Academic</option>
              <option value='location'>Location Focus</option>
            </select>

            <button type='submit' disabled={loading || !query.trim()} className='search-button'>
              {loading ? '🔍 Searching...' : '🔍 Search'}
            </button>
          </div>
        </div>

        {/* Search Suggestions */}
        {showSuggestions && suggestions.length > 0 && (
          <div className='search-suggestions'>
            {suggestions.map((suggestion, index) => (
              <div
                key={index}
                className='suggestion-item'
                role="button"
                tabIndex={0}
                onClick={() => handleSuggestionClick(suggestion)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    handleSuggestionClick(suggestion);
                  }
                }}
              >
                {suggestion}
              </div>
            ))}
          </div>
        )}
      </form>

      {/* Loading Indicator */}
      {loading && (
        <div className='loading-container'>
          <div className='loading-spinner'></div>
          <p>Searching across multiple sources...</p>
          <div className='loading-steps'>
            <div className='loading-step'>📊 Analyzing query</div>
            <div className='loading-step'>🔍 Searching web sources</div>
            <div className='loading-step'>📰 Checking news</div>
            <div className='loading-step'>📍 Processing location data</div>
            <div className='loading-step'>🗺️ Generating map visualization</div>
            <div className='loading-step'>🤖 Synthesizing results</div>
          </div>
        </div>
      )}

      {/* Search Results */}
      {renderSearchResults()}

      {/* Example Queries */}
      {!searchResults && !loading && (
        <div className='example-queries'>
          <h3>Try these example queries:</h3>
          <div className='example-grid'>
            <button
              className='example-query'
              onClick={() => handleSuggestionClick('best restaurants in Tokyo')}
            >
              🍣 Best restaurants in Tokyo
            </button>
            <button
              className='example-query'
              onClick={() => handleSuggestionClick('latest AI technology news')}
            >
              🤖 Latest AI technology news
            </button>
            <button
              className='example-query'
              onClick={() => handleSuggestionClick('weather in London today')}
            >
              🌤️ Weather in London today
            </button>
            <button
              className='example-query'
              onClick={() => handleSuggestionClick('directions to Central Park')}
            >
              📍 Directions to Central Park
            </button>
            <button
              className='example-query'
              onClick={() => handleSuggestionClick('research on quantum computing')}
            >
              📚 Research on quantum computing
            </button>
            <button
              className='example-query'
              onClick={() => handleSuggestionClick('hotels near Times Square')}
            >
              🏨 Hotels near Times Square
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default PerplexitySearch;
