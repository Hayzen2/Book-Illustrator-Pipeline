import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import {
  getAllProjects,
  createProject,
  createProjectWithFile,
  deleteProject,
} from '../api/projectApi';

const Homepage = () => {
  const navigate = useNavigate();

  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showCreateModal, setShowCreateModal] = useState(false);

  const [createMode, setCreateMode] = useState('text');

  const [title, setTitle] = useState('');
  const [bookText, setBookText] = useState('');
  const [file, setFile] = useState(null);

  const [creating, setCreating] = useState(false);

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await getAllProjects();

      setProjects(response.data.data || []);
    } catch (err) {
      console.error(err);

      setError(
        err.response?.data?.message ||
          'Failed to load your projects.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCreateProject = async (e) => {
    e.preventDefault();

    if (!title.trim()) {
      return;
    }

    try {
      setCreating(true);
      setError('');

      let response;

      if (createMode === 'text') {
        if (!bookText.trim()) {
          setError('Book content is required.');
          return;
        }

        response = await createProject({
          title: title.trim(),
          bookText: bookText.trim(),
        });
      } else {
        if (!file) {
          setError('Please select a TXT file.');
          return;
        }

        response = await createProjectWithFile(
          title.trim(),
          file
        );
      }

      const newProject = response.data.data;

      setProjects((prev) => [
        newProject,
        ...prev,
      ]);

      setTitle('');
      setBookText('');
      setFile(null);
      setShowCreateModal(false);

      /*
       * Your backend initializes the project context here,
       * but it may take some time before the project detail
       * information is available.
       *
       * We simply return to the project list.
       */
    } catch (err) {
      console.error(err);

      setError(
        err.response?.data?.message ||
          'Failed to create project.'
      );
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteProject = async (projectId, e) => {
    e.stopPropagation();

    const confirmed = window.confirm(
      'Are you sure you want to delete this project?'
    );

    if (!confirmed) {
      return;
    }

    try {
      await deleteProject(projectId);

      setProjects((prev) =>
        prev.filter((project) => project.id !== projectId)
      );
    } catch (err) {
      console.error(err);

      setError(
        err.response?.data?.message ||
          'Failed to delete project.'
      );
    }
  };

  const formatDate = (date) => {
    if (!date) return 'Unknown';

    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getStatusStyle = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-700';

      case 'PROCESSING':
        return 'bg-blue-100 text-blue-700';

      case 'FAILED':
        return 'bg-red-100 text-red-700';

      case 'DRAFT':
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-6 py-10">

      {/* Header */}
      <div className="mb-10 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Your Projects
          </h1>

          <p className="mt-2 text-gray-500">
            Create and manage your illustrated books.
          </p>
        </div>

        <button
          onClick={() => {
            setError('');
            setShowCreateModal(true);
          }}
          className="rounded-lg bg-black px-5 py-3 text-sm font-medium text-white transition hover:bg-gray-800"
        >
          + New Project
        </button>
      </div>

      {/* Error */}
      {error && (
        <div className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          {error}
        </div>
      )}

      {/* Loading */}
      {loading && (
        <div className="py-20 text-center text-gray-500">
          Loading projects...
        </div>
      )}

      {/* Empty state */}
      {!loading && projects.length === 0 && (
        <div className="rounded-2xl border border-dashed border-gray-300 bg-white px-6 py-20 text-center">
          <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-full bg-gray-100 text-2xl">
            📖
          </div>

          <h2 className="text-xl font-semibold text-gray-900">
            No projects yet
          </h2>

          <p className="mx-auto mt-2 max-w-md text-sm text-gray-500">
            Start your first illustrated book project by
            creating a new project.
          </p>

          <button
            onClick={() => setShowCreateModal(true)}
            className="mt-6 rounded-lg bg-black px-5 py-3 text-sm font-medium text-white hover:bg-gray-800"
          >
            Create your first project
          </button>
        </div>
      )}

      {/* Project grid */}
      {!loading && projects.length > 0 && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
          {projects.map((project) => (
            <div
              key={project.id}
              onClick={() =>
                navigate(`/projects/${project.id}`)
              }
              className="group cursor-pointer rounded-2xl border border-gray-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-md"
            >
              {/* Top */}
              <div className="flex items-start justify-between">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gray-100 text-xl">
                  📚
                </div>

                <button
                  onClick={(e) =>
                    handleDeleteProject(project.id, e)
                  }
                  className="rounded-lg px-3 py-2 text-sm text-gray-400 transition hover:bg-red-50 hover:text-red-500"
                >
                  Delete
                </button>
              </div>

              {/* Project info */}
              <div className="mt-6">
                <h2 className="truncate text-lg font-semibold text-gray-900">
                  {project.title}
                </h2>

                <p className="mt-2 text-sm text-gray-500">
                  Created {formatDate(project.createdAt)}
                </p>
              </div>

              {/* Status */}
              <div className="mt-6 flex items-center justify-between">
                <span
                  className={`rounded-full px-3 py-1 text-xs font-medium ${getStatusStyle(
                    project.status
                  )}`}
                >
                  {project.status}
                </span>

                <span className="text-sm font-medium text-gray-400 transition group-hover:text-gray-900">
                  View →
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">

            {/* Modal header */}
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold text-gray-900">
                  Create Project
                </h2>

                <p className="mt-1 text-sm text-gray-500">
                  Start creating your illustrated book.
                </p>
              </div>

              <button
                onClick={() => setShowCreateModal(false)}
                className="text-xl text-gray-400 hover:text-gray-700"
              >
                ×
              </button>
            </div>

            {/* Mode */}
            <div className="mt-6 grid grid-cols-2 rounded-lg bg-gray-100 p-1">
              <button
                type="button"
                onClick={() => {
                  setCreateMode('text');
                  setError('');
                }}
                className={`rounded-md py-2 text-sm font-medium ${
                  createMode === 'text'
                    ? 'bg-white shadow-sm'
                    : 'text-gray-500'
                }`}
              >
                Write text
              </button>

              <button
                type="button"
                onClick={() => {
                  setCreateMode('file');
                  setError('');
                }}
                className={`rounded-md py-2 text-sm font-medium ${
                  createMode === 'file'
                    ? 'bg-white shadow-sm'
                    : 'text-gray-500'
                }`}
              >
                Upload TXT
              </button>
            </div>

            <form
              onSubmit={handleCreateProject}
              className="mt-6 space-y-5"
            >
              {/* Title */}
              <div>
                <label className="mb-2 block text-sm font-medium text-gray-700">
                  Project title
                </label>

                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="My children's book"
                  className="w-full rounded-lg border border-gray-300 px-4 py-3 text-sm outline-none focus:border-black"
                />
              </div>

              {/* Text */}
              {createMode === 'text' && (
                <div>
                  <label className="mb-2 block text-sm font-medium text-gray-700">
                    Book content
                  </label>

                  <textarea
                    value={bookText}
                    onChange={(e) =>
                      setBookText(e.target.value)
                    }
                    rows={7}
                    placeholder="Once upon a time..."
                    className="w-full resize-none rounded-lg border border-gray-300 px-4 py-3 text-sm outline-none focus:border-black"
                  />
                </div>
              )}

              {/* File */}
              {createMode === 'file' && (
                <div>
                  <label className="mb-2 block text-sm font-medium text-gray-700">
                    TXT file
                  </label>

                  <input
                    type="file"
                    accept=".txt,text/plain"
                    onChange={(e) =>
                      setFile(e.target.files?.[0] || null)
                    }
                    className="w-full rounded-lg border border-gray-300 p-3 text-sm"
                  />
                </div>
              )}

              {/* Buttons */}
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() =>
                    setShowCreateModal(false)
                  }
                  className="rounded-lg px-5 py-3 text-sm font-medium text-gray-600 hover:bg-gray-100"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={creating}
                  className="rounded-lg bg-black px-5 py-3 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {creating
                    ? 'Creating...'
                    : 'Create Project'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Homepage;