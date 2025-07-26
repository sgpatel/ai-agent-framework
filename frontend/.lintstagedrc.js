module.exports = {
  '*.{js,jsx}': [
    'eslint --fix',
    'prettier --write',
    'git add'
  ],
  '*.{css,scss,md}': [
    'prettier --write',
    'git add'
  ],
  '*.{json}': [
    'prettier --write',
    'git add'
  ]
};

