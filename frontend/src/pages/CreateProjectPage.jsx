import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  createProject,
  createProjectFromTxt,
} from '../api/projectApi';

const CreateProjectPage = () => {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [bookText, setBookText] = useState('');
  const [file, setFile] = useState(null);

  const [mode, setMode] = useState('text');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError('');

    if (!title.trim()) {
      setError('Please enter a project title.');
      return;
    }

    if (mode === 'text' && !bookText.trim()) {
      setError('Please enter your book content.');
      return;
    }

    if (mode === 'file' && !file) {
      setError('Please select a TXT file.');
      return;
    }

    try {
      setLoading(true);

      let response;

      if (mode === 'text') {
        response = await createProject(
          title,
          bookText
        );
      } else {
        response = await createProjectFromTxt(
          title,
          file
        );
      }

      const project = response.data;

      navigate(`/projects/${project.id}`);
    } catch (err) {
      console.error(err);
      setError('Failed to create project.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-80px)]
                    bg-gray-50">

      <div className="max-w-3xl mx-auto px-6 py-12">

        <button
          onClick={() => navigate('/projects')}
          className="text-sm text-gray-500
                     hover:text-indigo-600"
        >
          ← Back to projects
        </button>

        <div className="mt-6">
          <h1 className="text-4xl font-bold text-gray-900">
            Create a New Project
          </h1>

          <p className="mt-2 text-gray-500">
            Turn your story into an illustrated book.
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="mt-8 bg-white rounded-2xl
                     border border-gray-200 p-8"
        >

          {/* Title */}
          <div>
            <label className="block text-sm font-semibold
                              text-gray-700 mb-2">
              Project title
            </label>

            <input
              type="text"
              value={title}
              onChange={(e) =>
                setTitle(e.target.value)
              }
              placeholder="e.g. The Little Star"
              className="w-full px-4 py-3 rounded-xl
                         border border-gray-300
                         focus:outline-none
                         focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          {/* Mode */}
          <div className="mt-8">

            <label className="block text-sm font-semibold
                              text-gray-700 mb-3">
              Book content
            </label>

            <div className="flex gap-3 mb-5">

              <button
                type="button"
                onClick={() => setMode('text')}
                className={`px-4 py-2 rounded-lg
                  font-medium
                  ${
                    mode === 'text'
                      ? 'bg-indigo-600 text-white'
                      : 'bg-gray-100 text-gray-600'
                  }`}
              >
                Write text
              </button>

              <button
                type="button"
                onClick={() => setMode('file')}
                className={`px-4 py-2 rounded-lg
                  font-medium
                  ${
                    mode === 'file'
                      ? 'bg-indigo-600 text-white'
                      : 'bg-gray-100 text-gray-600'
                  }`}
              >
                Upload TXT
              </button>

            </div>

            {mode === 'text' ? (
              <textarea
                value={bookText}
                onChange={(e) =>
                  setBookText(e.target.value)
                }
                placeholder="Write or paste your story here..."
                rows={14}
                className="w-full px-4 py-3 rounded-xl
                           border border-gray-300
                           resize-y
                           focus:outline-none
                           focus:ring-2 focus:ring-indigo-500"
              />
            ) : (
              <div className="border-2 border-dashed
                              border-gray-300 rounded-xl
                              p-10 text-center">

                <div className="text-4xl">
                  📄
                </div>

                <p className="mt-3 text-gray-600">
                  Select a TXT file
                </p>

                <input
                  type="file"
                  accept=".txt,text/plain"
                  onChange={(e) =>
                    setFile(e.target.files?.[0] || null)
                  }
                  className="mt-5"
                />

                {file && (
                  <p className="mt-3 text-sm
                                text-indigo-600">
                    {file.name}
                  </p>
                )}

              </div>
            )}
          </div>

          {/* Error */}
          {error && (
            <div className="mt-6 px-4 py-3 rounded-xl
                            bg-red-50 text-red-600 text-sm">
              {error}
            </div>
          )}

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className="mt-8 w-full py-3 rounded-xl
                       bg-indigo-600 text-white
                       font-semibold
                       hover:bg-indigo-700
                       disabled:opacity-50
                       disabled:cursor-not-allowed"
          >
            {loading
              ? 'Creating project...'
              : 'Create Project'}
          </button>

        </form>
      </div>
    </div>
  );
};

export default CreateProjectPage;