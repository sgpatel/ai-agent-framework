import React, { useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { Form, Button, Alert, Spinner } from 'react-bootstrap';
import JsonView from '@uiw/react-json-view';

export default function TaskForm() {
  const { submitTask, loading } = useAppContext();
  const [type, setType] = useState('');
  const [desc, setDesc] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleSubmit = async e => {
    e.preventDefault();
    try {
      const res = await submitTask({ type, description: desc, parameters: {} });
      setResult(res);
      setError(null);
    } catch (error) {
      console.error('Error submitting task:', error);
      setError(error.message || 'Failed to submit task');
    }
  };

  return (
    <>
      <h2>New Task</h2>
      <Form onSubmit={handleSubmit}>
        <Form.Group className='mb-3'>
          <Form.Label>Type</Form.Label>
          <Form.Control value={type} onChange={e => setType(e.target.value)} required />
        </Form.Group>
        <Form.Group className='mb-3'>
          <Form.Label>Description</Form.Label>
          <Form.Control
            as='textarea'
            rows={3}
            value={desc}
            onChange={e => setDesc(e.target.value)}
            required
          />
        </Form.Group>
        <Button type='submit' disabled={loading}>
          {loading ? <Spinner animation='border' size='sm' /> : 'Submit'}
        </Button>
      </Form>
      {result && (
        <Alert className='mt-3' variant='success'>
          <JsonView value={result} />
        </Alert>
      )}
      {error && (
        <Alert className='mt-3' variant='danger'>
          {error}
        </Alert>
      )}
    </>
  );
}
