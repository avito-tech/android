#!/usr/bin/env sh

# Based on https://gohugo.io/hosting-and-deployment/hosting-on-github/

set -euf -o

git config --global core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no'
git config --global user.name "$GITHUB_GIT_USER_NAME";
git config --global user.email "$GITHUB_GIT_USER_EMAIL";
mkdir -p "${HOME}"/.ssh

removeGhPagesWorktree() {
    rm -rf gh-pages-generated
    git worktree prune
}

echo "Deleting old worktree..."
removeGhPagesWorktree

echo "Checking out gh-pages branch into temporary directory..."
git worktree add -B gh-pages gh-pages-generated origin/gh-pages

echo "Generating site..."
hugo --cleanDestinationDir --minify --theme book --source docs

echo "Updating gh-pages branch..."
cd gh-pages-generated

echo "Removing previously generated files..."
git rm -r -- *
echo "Replacing generated files..."
cp -RT ../docs/public .

touch README.md
echo "These file are auto-generated by hugo. See docs/publish.sh in the develop." > README.md

git add --all
git status

echo "Pushing changes..."
# Amend history because we don't want to store a huge history of generated files
git commit --amend -m "auto-generated files" && git push --force-with-lease

echo "Deleting generated files..."
removeGhPagesWorktree
