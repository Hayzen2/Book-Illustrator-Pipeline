import React from 'react';

const Footer = () => {
  return (
    <footer className="border-t border-gray-200 bg-white">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-6">
        <p className="text-sm text-gray-500">
          © {new Date().getFullYear()} Book Illustrator
        </p>

        <p className="text-sm text-gray-400">
          Create beautiful illustrated stories
        </p>
      </div>
    </footer>
  );
};

export default Footer;