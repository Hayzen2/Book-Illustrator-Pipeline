import React, { useEffect, useState, useCallback } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getPipelineStatus, executeProjectStep } from '../api/aiPipelineApi';

import { SectionTitle, StatusBadge, EmptySection } from '../components/project/SharedUI';
import { PipelineProgress } from '../components/project/PipelineProgress';
import { ActionPanel } from '../components/project/ActionPanel';
import { CharacterCard, ChapterCard } from '../components/project/MediaCards';

const STEP_ORDER = ['STYLE', 'CHARACTERS', 'PORTRAITS', 'CHAPTERS', 'ILLUSTRATIONS'];

const ProjectDetailPage = () => {
  const { projectId } = useParams();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [customStyle, setCustomStyle] = useState('');
  const [executing, setExecuting] = useState(false);

  const fetchProjectStatus = useCallback(async () => {
    try {
      const response = await getPipelineStatus(projectId);
      setProject(response.data.data);
      setError('');
    } catch (err) {
      console.error(err);
      setError('Unable to load project status.');
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    fetchProjectStatus();
  }, [fetchProjectStatus]);

  // Polling mechanism
  useEffect(() => {
    if (!project || !project.steps) return;
    const isProcessing = project.steps.some(s => s.status === 'IN_PROGRESS');

    let pollInterval;
    if (isProcessing) {
      pollInterval = setInterval(fetchProjectStatus, 3000);
    }
    return () => { if (pollInterval) clearInterval(pollInterval); };
  }, [project, fetchProjectStatus]);

  const handleExecuteStep = async (stepName) => {
    try {
      setExecuting(true);
      await executeProjectStep(projectId, stepName, customStyle);
      setCustomStyle(''); 
      await fetchProjectStatus(); 
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || 'Failed to execute step.');
    } finally {
      setExecuting(false);
    }
  };

  if (loading && !project) return <div className="min-h-[70vh] flex items-center justify-center text-gray-500">Loading project...</div>;
  
  if (error && !project) {
    return (
      <div className="max-w-5xl mx-auto px-6 py-10">
        <Link to="/projects" className="text-indigo-600">← Back to projects</Link>
        <div className="mt-8 p-6 bg-red-50 text-red-600 rounded-xl">{error}</div>
      </div>
    );
  }

  if (!project) return null;

  const activeStep = STEP_ORDER.map(name => project.steps?.find(s => s.stepName === name))
                               .find(step => step && step.status !== 'COMPLETED') || null;

  return (
    <div className="min-h-[calc(100vh-80px)] bg-gray-50 pb-20">
      <div className="max-w-7xl mx-auto px-6 py-10">
        
        <Link to="/projects" className="text-sm font-medium text-gray-500 hover:text-indigo-600">
          ← Back to projects
        </Link>

        {/* Header */}
        <div className="mt-6 bg-white rounded-2xl border border-gray-200 p-8 shadow-sm">
          <div className="flex items-center gap-3">
            <h1 className="text-4xl font-bold text-gray-900">{project.title}</h1>
            <StatusBadge status={project.globalStatus} />
          </div>
          {project.artStyle && (
            <div className="mt-4 p-4 bg-indigo-50 border border-indigo-100 rounded-lg text-indigo-900 text-sm">
              <strong>Active Art Style:</strong> {project.artStyle}
            </div>
          )}
        </div>

        {/* Main Interface */}
        <section className="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2">
            <SectionTitle title="Pipeline Progress" subtitle="Track the end-to-end generation." />
            <div className="mt-5 bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
              <PipelineProgress steps={project.steps} />
            </div>
          </div>

          <div className="lg:col-span-1">
            <SectionTitle title="Actions" subtitle="Drive the AI generation." />
            <div className="mt-5 bg-white rounded-2xl border border-gray-200 p-6 shadow-sm sticky top-6">
              <ActionPanel 
                activeStep={activeStep} 
                executing={executing} 
                onExecute={handleExecuteStep} 
                customStyle={customStyle} 
                setCustomStyle={setCustomStyle} 
              />
            </div>
          </div>
        </section>

        {/* Results */}
        <section className="mt-12">
          <SectionTitle title="Characters" subtitle={`${project.characters?.length || 0} characters`} />
          {project.characters?.length > 0 ? (
            <div className="mt-5 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">
              {project.characters.map((char) => <CharacterCard key={char.id} character={char} />)}
            </div>
          ) : <EmptySection text="No characters generated yet." />}
        </section>

        <section className="mt-12">
          <SectionTitle title="Chapters" subtitle={`${project.chapters?.length || 0} chapters`} />
          {project.chapters?.length > 0 ? (
            <div className="mt-5 space-y-6">
              {project.chapters.map((chap, idx) => <ChapterCard key={chap.id} chapter={chap} index={idx} />)}
            </div>
          ) : <EmptySection text="No chapters generated yet." />}
        </section>

      </div>
    </div>
  );
};

export default ProjectDetailPage;