import React from 'react';
import { useNavigate } from 'react-router-dom';
import './HomePage.css';

const HomePage = () => {
  const navigate = useNavigate();

  return (
    <main className="home-page">
      <div className="home-container">

        {/* Hero */}
        <section className="home-hero">
          <div className="hero-content">
            <span className="hero-badge">
              BOOK ILLUSTRATION STUDIO
            </span>

            <h1>
              Turn your stories into
              <span> beautiful illustrations.</span>
            </h1>

            <p>
              Create consistent characters, portraits, chapter scenes,
              and illustrations from your book using AI-powered generation.
            </p>

            <div className="hero-actions">
              <button
                className="gd-btn gd-btn-primary"
                onClick={() => navigate('/projects/new')}
              >
                + New project
                <span>→</span>
              </button>

              <button
                className="gd-btn gd-btn-secondary"
                onClick={() => navigate('/projects')}
              >
                View projects
              </button>
            </div>
          </div>

          <div className="hero-art">
            <div className="hero-art-inner">
              <span className="hero-art-label">YOUR STORY</span>

              <div className="hero-book">
                <div className="book-line book-line-1" />
                <div className="book-line book-line-2" />
                <div className="book-line book-line-3" />
              </div>

              <span className="hero-art-caption">
                From words to worlds.
              </span>
            </div>
          </div>
        </section>

        {/* Getting started */}
        <section className="getting-started">
          <div className="section-heading">
            <div>
              <h2>How it works</h2>
              <p>
                Build your illustration project step by step.
              </p>
            </div>
          </div>

          <div className="steps-grid">

            <div className="feature-card">
              <div className="gd-num-square">1</div>
              <h3>Upload your book</h3>
              <p>
                Provide your book text by uploading a text file or
                pasting the content directly.
              </p>
            </div>

            <div className="feature-card">
              <div className="gd-num-square">2</div>
              <h3>Define your style</h3>
              <p>
                Choose an art style or let the AI create one based
                on the tone of your story.
              </p>
            </div>

            <div className="feature-card">
              <div className="gd-num-square">3</div>
              <h3>Generate characters</h3>
              <p>
                Create consistent characters and portraits that
                belong to the same visual world.
              </p>
            </div>

            <div className="feature-card">
              <div className="gd-num-square">4</div>
              <h3>Illustrate chapters</h3>
              <p>
                Generate scenes and illustrations based on your
                story and established visual style.
              </p>
            </div>

          </div>
        </section>

        {/* Workflow */}
        <section className="workflow-section">
          <div className="workflow-card">
            <div className="workflow-content">
              <span className="workflow-label">
                ILLUSTRATION WORKFLOW
              </span>

              <h2>
                Keep your entire book
                <br />
                in one visual language.
              </h2>

              <p>
                Book Illustrator keeps your style and characters
                consistent throughout the entire generation process.
              </p>

              <button
                className="gd-btn gd-btn-primary"
                onClick={() => navigate('/projects/new')}
              >
                Start illustrating
                <span>→</span>
              </button>
            </div>

            <div className="workflow-steps">

              <div className="workflow-step">
                <div className="workflow-number done">✓</div>
                <div>
                  <strong>Style</strong>
                  <span>Visual direction</span>
                </div>
              </div>

              <div className="workflow-line" />

              <div className="workflow-step">
                <div className="workflow-number">2</div>
                <div>
                  <strong>Characters</strong>
                  <span>Consistent cast</span>
                </div>
              </div>

              <div className="workflow-line" />

              <div className="workflow-step">
                <div className="workflow-number">3</div>
                <div>
                  <strong>Portraits</strong>
                  <span>Character portraits</span>
                </div>
              </div>

              <div className="workflow-line" />

              <div className="workflow-step">
                <div className="workflow-number">4</div>
                <div>
                  <strong>Chapters</strong>
                  <span>Scene prompts</span>
                </div>
              </div>

              <div className="workflow-line" />

              <div className="workflow-step">
                <div className="workflow-number">5</div>
                <div>
                  <strong>Illustrations</strong>
                  <span>Final artwork</span>
                </div>
              </div>

            </div>
          </div>
        </section>

      </div>
    </main>
  );
};

export default HomePage;