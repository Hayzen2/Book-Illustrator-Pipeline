import React from 'react';

// Replace with your actual backend mapping URL logic if needed
const getImageUrl = (path) => `http://localhost:8080/api/images?path=${encodeURIComponent(path)}`;

export const CharacterCard = ({ character }) => (
  <div className="bg-white rounded-2xl border border-gray-200 overflow-hidden hover:shadow-md transition">
    <div className="aspect-square bg-gray-100">
      {character.portraitImagePath ? (
        <img
          src={getImageUrl(character.portraitImagePath)}
          alt={character.name}
          className="w-full h-full object-cover"
        />
      ) : (
        <div className="w-full h-full flex items-center justify-center text-4xl">👤</div>
      )}
    </div>
    <div className="p-4">
      <h3 className="font-semibold text-gray-900">{character.name}</h3>
      {character.imagePrompt && (
        <p className="mt-2 text-xs text-gray-500 line-clamp-3" title={character.imagePrompt}>
          {character.imagePrompt}
        </p>
      )}
    </div>
  </div>
);

export const ChapterCard = ({ chapter, index }) => (
  <div className="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-sm">
    <div className="grid md:grid-cols-2">
      <div className="min-h-[280px] bg-gray-100">
        {chapter.illustrationImagePath ? (
          <img
            src={getImageUrl(chapter.illustrationImagePath)}
            alt={chapter.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="h-full min-h-[280px] flex items-center justify-center text-gray-400">
            Illustration pending
          </div>
        )}
      </div>
      <div className="p-8">
        <span className="text-sm font-semibold text-indigo-600">Chapter {index + 1}</span>
        <h3 className="mt-2 text-2xl font-bold text-gray-900">{chapter.name}</h3>
        {chapter.illustrationPrompt && (
          <div className="mt-6">
            <h4 className="text-sm font-semibold text-gray-700">Illustration Prompt</h4>
            <p className="mt-2 text-sm leading-6 text-gray-500">{chapter.illustrationPrompt}</p>
          </div>
        )}
      </div>
    </div>
  </div>
);