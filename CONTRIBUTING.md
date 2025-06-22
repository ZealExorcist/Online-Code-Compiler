# Contributing to Online Code Compiler

Thank you for considering contributing to the Online Code Compiler! We welcome contributions from the community.

## 🤝 How to Contribute

### Reporting Bugs

1. **Check existing issues** first to avoid duplicates
2. **Use the bug report template** when creating new issues
3. **Provide detailed information**:
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details (OS, browser, etc.)
   - Screenshots if applicable

### Suggesting Features

1. **Check existing feature requests** to avoid duplicates
2. **Use the feature request template**
3. **Explain the use case** and why it would be valuable
4. **Consider backwards compatibility**

### Code Contributions

#### Prerequisites
- Node.js (v18+)
- Java 17+
- Docker
- Git
- MongoDB (local or Atlas)

#### Development Setup

1. **Fork and clone the repository**
   ```bash
   git clone https://github.com/your-username/online-code-compiler.git
   cd online-code-compiler
   ```

2. **Set up development environment**
   ```bash
   # Run setup script
   chmod +x setup.sh
   ./setup.sh
   ```

3. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

#### Code Style Guidelines

**Frontend (Vue.js/TypeScript):**
- Use TypeScript for type safety
- Follow Vue.js composition API patterns
- Use meaningful component and variable names
- Add proper JSDoc comments for functions
- Follow the existing file structure

**Backend (Spring Boot/Java):**
- Follow Java naming conventions (camelCase for methods, PascalCase for classes)
- Use Spring Boot best practices
- Add proper Javadoc comments
- Follow REST API conventions
- Use appropriate HTTP status codes

**General:**
- Use meaningful commit messages
- Write self-documenting code
- Add comments for complex logic
- Follow existing patterns in the codebase

#### Testing

**Frontend Testing:**
```bash
cd frontend
npm run test
```

**Backend Testing:**
```bash
cd backend
./mvnw test
```

#### Pull Request Process

1. **Before submitting:**
   - Ensure all tests pass
   - Update documentation if needed
   - Test your changes thoroughly
   - Remove any debugging code

2. **PR Requirements:**
   - Clear, descriptive title
   - Detailed description of changes
   - Link to related issues
   - Screenshots for UI changes

3. **Review Process:**
   - All PRs require at least one review
   - Address feedback promptly
   - Keep your branch up to date

#### Commit Message Format

```
type(scope): brief description

Detailed explanation of what changed and why.

Closes #123
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(editor): add syntax highlighting for Rust

Added Monaco Editor support for Rust language with proper 
syntax highlighting and auto-completion.

Closes #45

fix(auth): prevent logout on anonymous 401 errors

Fixed issue where anonymous users were being logged out
when accessing protected endpoints.

Closes #67
```

## 🛠️ Development Guidelines

### Adding New Programming Languages

1. **Create Docker image**
   - Add `Dockerfile.{language}` in `docker/images/`
   - Test the container execution

2. **Update backend**
   - Add language support in `ExecutionService.java`
   - Add language configuration
   - Test execution endpoints

3. **Update frontend**
   - Add language option in `CodeEditor.vue`
   - Add default code template
   - Add syntax highlighting support

4. **Documentation**
   - Update README.md
   - Add usage examples

### Security Considerations

- **Never trust user input** - Always validate and sanitize
- **Use secure defaults** - Fail securely when possible
- **Follow principle of least privilege**
- **Keep dependencies updated**
- **Review Docker configurations** for security

### Performance Guidelines

- **Frontend optimization:**
  - Use lazy loading for components
  - Optimize bundle size
  - Minimize API calls

- **Backend optimization:**
  - Use appropriate database indexes
  - Implement caching where beneficial
  - Optimize Docker container startup

## 📝 Issue Templates

### Bug Report Template
```markdown
**Description**
A clear description of the bug.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. See error

**Expected Behavior**
What you expected to happen.

**Screenshots**
If applicable, add screenshots.

**Environment:**
- OS: [e.g. Windows 11]
- Browser: [e.g. Chrome 96]
- Version: [e.g. v1.0.0]
```

### Feature Request Template
```markdown
**Is your feature request related to a problem?**
A clear description of what the problem is.

**Describe the solution you'd like**
A clear description of what you want to happen.

**Describe alternatives you've considered**
Other solutions you've considered.

**Additional context**
Any other context or screenshots.
```

## 🎯 Areas for Contribution

### High Priority
- [ ] Mobile responsiveness improvements
- [ ] Performance optimizations
- [ ] Additional language support
- [ ] Enhanced error messages
- [ ] Accessibility improvements

### Medium Priority
- [ ] Code templates and snippets
- [ ] Advanced editor features
- [ ] User experience enhancements
- [ ] Documentation improvements

### Good First Issues
- [ ] UI/UX polish
- [ ] Documentation fixes
- [ ] Simple bug fixes
- [ ] Test coverage improvements

## 🏆 Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- GitHub contributors page

## 📞 Getting Help

- **Discord**: Join our community Discord
- **GitHub Discussions**: For questions and discussions
- **Email**: project-maintainers@example.com

## 📜 Code of Conduct

This project follows the [Contributor Covenant](https://www.contributor-covenant.org/) Code of Conduct. By participating, you are expected to uphold this code.

### Our Pledge

We are committed to making participation in this project a harassment-free experience for everyone, regardless of age, body size, disability, ethnicity, gender identity and expression, level of experience, nationality, personal appearance, race, religion, or sexual identity and orientation.

## ⚖️ License

By contributing to this project, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to Online Code Compiler! 🚀
