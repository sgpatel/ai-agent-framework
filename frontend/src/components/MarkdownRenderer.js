import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import './MarkdownRenderer.css';

const CodeBlock = ({ children, className, ...props }) => {
  const [copied, setCopied] = useState(false);
  const language = className?.replace('language-', '') || 'text';

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(children);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy code:', err);
    }
  };

  return (
    <div className='code-block-container'>
      <div className='code-block-header'>
        <span className='code-language'>{language}</span>
        <button
          className={`copy-button ${copied ? 'copied' : ''}`}
          onClick={copyToClipboard}
          title='Copy to clipboard'
        >
          {copied ? (
            <>
              <svg
                width='16'
                height='16'
                viewBox='0 0 24 24'
                fill='none'
                stroke='currentColor'
                strokeWidth='2'
              >
                <polyline points='20,6 9,17 4,12'></polyline>
              </svg>
              Copied!
            </>
          ) : (
            <>
              <svg
                width='16'
                height='16'
                viewBox='0 0 24 24'
                fill='none'
                stroke='currentColor'
                strokeWidth='2'
              >
                <rect x='9' y='9' width='13' height='13' rx='2' ry='2'></rect>
                <path d='m5,15H4a2,2 0 0,1 -2,-2V4a2,2 0 0,1 2,-2H13a2,2 0 0,1 2,2v1'></path>
              </svg>
              Copy
            </>
          )}
        </button>
      </div>
      <pre className={`code-block language-${language}`}>
        <code {...props}>{children}</code>
      </pre>
    </div>
  );
};

const MarkdownRenderer = ({ content, className = '' }) => {
  const components = {
    code: ({ inline, className, children, ...props }) => {
      if (inline) {
        return (
          <code className='inline-code' {...props}>
            {children}
          </code>
        );
      }
      return (
        <CodeBlock className={className} {...props}>
          {children}
        </CodeBlock>
      );
    },
    h1: ({ children }) => <h1 className='md-h1'>{children}</h1>,
    h2: ({ children }) => <h2 className='md-h2'>{children}</h2>,
    h3: ({ children }) => <h3 className='md-h3'>{children}</h3>,
    h4: ({ children }) => <h4 className='md-h4'>{children}</h4>,
    p: ({ children }) => <p className='md-paragraph'>{children}</p>,
    ul: ({ children }) => <ul className='md-list'>{children}</ul>,
    ol: ({ children }) => <ol className='md-ordered-list'>{children}</ol>,
    li: ({ children }) => <li className='md-list-item'>{children}</li>,
    blockquote: ({ children }) => <blockquote className='md-blockquote'>{children}</blockquote>,
    table: ({ children }) => <table className='md-table'>{children}</table>,
    thead: ({ children }) => <thead className='md-thead'>{children}</thead>,
    tbody: ({ children }) => <tbody className='md-tbody'>{children}</tbody>,
    tr: ({ children }) => <tr className='md-tr'>{children}</tr>,
    th: ({ children }) => <th className='md-th'>{children}</th>,
    td: ({ children }) => <td className='md-td'>{children}</td>,
    strong: ({ children }) => <strong className='md-strong'>{children}</strong>,
    em: ({ children }) => <em className='md-emphasis'>{children}</em>,
    a: ({ href, children }) => (
      <a href={href} className='md-link' target='_blank' rel='noopener noreferrer'>
        {children}
      </a>
    ),
  };

  return (
    <div className={`markdown-renderer ${className}`}>
      <ReactMarkdown remarkPlugins={[remarkGfm]} components={components}>
        {content}
      </ReactMarkdown>
    </div>
  );
};

export default MarkdownRenderer;
