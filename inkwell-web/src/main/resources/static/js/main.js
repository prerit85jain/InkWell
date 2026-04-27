/**
 * InkWell - Frontend Interactivity
 * Handles dark mode, navigation, skeletons, reading progress, etc.
 */

(function() {
  'use strict';

  // ============================================
  // Theme Management
  // ============================================
  const Theme = {
    STORAGE_KEY = 'inkwell-theme',
    
    init() {
      const saved = localStorage.getItem(this.STORAGE_KEY);
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      
      if (saved) {
        this.set(saved);
      } else if (prefersDark) {
        this.set('dark');
      }
      
      this.bindEvents();
    },
    
    set(theme) {
      document.documentElement.setAttribute('data-theme', theme);
      localStorage.setItem(this.STORAGE_KEY, theme);
    },
    
    toggle() {
      const current = document.documentElement.getAttribute('data-theme');
      this.set(current === 'dark' ? 'light' : 'dark');
    },
    
    bindEvents() {
      const toggle = document.querySelector('.theme-toggle');
      if (toggle) {
        toggle.addEventListener('click', () => this.toggle());
      }
      
      // Listen for system theme changes
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
        if (!localStorage.getItem(this.STORAGE_KEY)) {
          this.set(e.matches ? 'dark' : 'light');
        }
      });
    }
  };

  // ============================================
  // Mobile Navigation
  // ============================================
  const MobileNav = {
    init() {
      this.toggle = document.querySelector('.navbar-toggle');
      this.nav = document.querySelector('.navbar-nav');
      
      if (this.toggle && this.nav) {
        this.toggle.addEventListener('click', () => this.toggleNav());
        document.addEventListener('click', e => {
          if (!this.toggle.contains(e.target) && !this.nav.contains(e.target)) {
            this.close();
          }
        });
      }
    },
    
    toggleNav() {
      this.nav.classList.toggle('active');
      this.toggle.setAttribute('aria-expanded', 
        this.nav.classList.contains('active'));
    },
    
    close() {
      this.nav.classList.remove('active');
      this.toggle.setAttribute('aria-expanded', 'false');
    }
  };

  // ============================================
  // Reading Progress Bar
  // ============================================
  const ReadingProgress = {
    progressBar: null,
    
    init() {
      this.progressBar = document.querySelector('.reading-progress-bar');
      if (!this.progressBar) return;
      
      window.addEventListener('scroll', () => this.update());
      this.update();
    },
    
    update() {
      const docHeight = document.documentElement.scrollHeight - window.innerHeight;
      const progress = (window.scrollY / docHeight) * 100;
      this.progressBar.style.width = Math.min(100, Math.max(0, progress)) + '%';
    }
  };

  // ============================================
  // Skeleton Loaders
  // ============================================
  const Skeletons = {
    init() {
      this.createPostSkeletons();
      this.observeSkeletons();
    },
    
    createPostSkeletons() {
      const containers = document.querySelectorAll('.post-list');
      containers.forEach(container => {
        if (container.dataset.loaded === 'true') return;
        
        const count = parseInt(container.dataset.skeletonCount) || 6;
        container.innerHTML = '';
        
        for (let i = 0; i < count; i++) {
          container.innerHTML += this.postSkeleton;
        }
      });
    },
    
    postSkeleton: `
      <article class="post-card-skeleton">
        <div class="skeleton skeleton-image"></div>
        <div class="card-body">
          <div class="skeleton skeleton-title"></div>
          <div class="skeleton skeleton-text" style="width: 90%"></div>
          <div class="skeleton skeleton-text" style="width: 75%"></div>
          <div class="flex items-center gap-md" style="margin-top: 1rem">
            <div class="skeleton skeleton-avatar"></div>
            <div class="skeleton skeleton-text" style="width: 100px"></div>
          </div>
        </div>
      </article>
    `,
    
    observeSkeletons() {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.loadContent(entry.target);
            observer.unobserve(entry.target);
          }
        });
      }, { rootMargin: '100px' });
      
      document.querySelectorAll('.post-list').forEach(el => {
        observer.observe(el);
      });
    },
    
    loadContent(container) {
      // Replace skeletons with actual content
      // This is typically called after fetching data from API
      container.dataset.loaded = 'true';
      // Content loading logic handled by individual components
    }
  };

  // ============================================
  // Like Button Handler
  // ============================================
  const LikeButton = {
    init() {
      document.addEventListener('click', e => {
        const btn = e.target.closest('.like-button');
        if (!btn) return;
        
        e.preventDefault();
        this.toggle(btn);
      });
    },
    
    async toggle(button) {
      const postId = button.dataset.postId;
      const liked = button.classList.contains('liked');
      const endpoint = liked ? `/api/posts/${postId}/unlike` : `/api/posts/${postId}/like`;
      const icon = button.querySelector('svg');
      
      // Optimistic update
      button.classList.toggle('liked');
      button.classList.add('loading');
      
      try {
        const response = await fetch(endpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-Token': this.getCsrfToken()
          }
        });
        
        if (!response.ok) {
          throw new Error('Failed to update like');
        }
        
        const data = await response.json();
        this.updateCount(button, data.likesCount);
      } catch (error) {
        // Revert on error
        button.classList.toggle('liked');
        console.error('Like error:', error);
      } finally {
        button.classList.remove('loading');
      }
    },
    
    updateCount(button, count) {
      const countEl = button.querySelector('.like-count');
      if (countEl) {
        countEl.textContent = this.formatCount(count);
      }
    },
    
    formatCount(count) {
      if (count >= 1000000) {
        return (count / 1000000).toFixed(1) + 'M';
      }
      if (count >= 1000) {
        return (count / 1000).toFixed(1) + 'K';
      }
      return count.toString();
    },
    
    getCsrfToken() {
      const meta = document.querySelector('meta[name="csrf-token"]');
      return meta ? meta.content : '';
    }
  };

  // ============================================
  // Toast Notifications
  // ============================================
  const Toast = {
    container: null,
    
    init() {
      this.container = document.querySelector('.toast-container');
      if (!this.container) {
        this.container = document.createElement('div');
        this.container.className = 'toast-container';
        document.body.appendChild(this.container);
      }
    },
    
    show(message, type = 'info', duration = 5000) {
      const toast = document.createElement('div');
      toast.className = `toast alert-${type}`;
      toast.innerHTML = `
        <span>${message}</span>
        <button class="toast-close" aria-label="Close">&times;</button>
      `;
      
      toast.querySelector('.toast-close').addEventListener('click', () => {
        this.hide(toast);
      });
      
      this.container.appendChild(toast);
      
      if (duration > 0) {
        setTimeout(() => this.hide(toast), duration);
      }
    },
    
    hide(toast) {
      toast.classList.add('hiding');
      setTimeout(() => toast.remove(), 300);
    }
  };

  // ============================================
  // Forms
  // ============================================
  const Forms = {
    init() {
      this.initValidation();
      this.initNewsletter();
    },
    
    initValidation() {
      document.addEventListener('submit', e => {
        const form = e.target;
        if (!form.classList.contains('validate')) return;
        
        if (!this.validateForm(form)) {
          e.preventDefault();
        }
      });
      
      document.addEventListener('input', e => {
        const input = e.target;
        if (!input.classList.contains('validate-input')) return;
        
        this.validateInput(input);
      });
    },
    
    validateForm(form) {
      let valid = true;
      form.querySelectorAll('.validate-input').forEach(input => {
        if (!this.validateInput(input)) {
          valid = false;
        }
      });
      return valid;
    },
    
    validateInput(input) {
      const group = input.closest('.form-group') || input.parentElement;
      const error = group.querySelector('.form-error');
      let isValid = true;
      let message = '';
      
      if (input.required && !input.value.trim()) {
        isValid = false;
        message = 'This field is required';
      } else if (input.type === 'email' && input.value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(input.value)) {
          isValid = false;
          message = 'Please enter a valid email address';
        }
      } else if (input.minLength && input.value.length < input.minLength) {
        isValid = false;
        message = `Must be at least ${input.minLength} characters`;
      }
      
      if (error) {
        error.textContent = message;
        error.style.display = message ? 'block' : 'none';
      }
      
      input.classList.toggle('invalid', !isValid);
      return isValid;
    },
    
    initNewsletter() {
      const form = document.querySelector('.newsletter-form');
      if (!form) return;
      
      form.addEventListener('submit', async e => {
        e.preventDefault();
        
        const input = form.querySelector('input[type="email"]');
        const button = form.querySelector('button');
        const email = input.value;
        
        if (!email) return;
        
        button.disabled = true;
        button.textContent = 'Subscribing...';
        
        try {
          const response = await fetch('/api/newsletter/subscribe', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
          });
          
          const data = await response.json();
          
          if (response.ok) {
            Toast.show('Please check your email to confirm subscription', 'success');
            input.value = '';
          } else {
            Toast.show(data.message || 'Subscription failed', 'error');
          }
        } catch (error) {
          Toast.show('Something went wrong. Please try again.', 'error');
        } finally {
          button.disabled = false;
          button.textContent = 'Subscribe';
        }
      });
    }
  };

  // ============================================
  // Comment Form
  // ============================================
  const Comments = {
    init() {
      this.form = document.querySelector('.comment-form');
      if (!this.form) return;
      
      this.form.addEventListener('submit', e => this.handleSubmit(e));
    },
    
    async handleSubmit(e) {
      e.preventDefault();
      
      const postId = this.form.dataset.postId;
      const content = this.form.querySelector('textarea').value;
      const parentId = this.form.dataset.replyTo;
      
      if (!content.trim()) return;
      
      const submitBtn = this.form.querySelector('button[type="submit"]');
      submitBtn.disabled = true;
      
      try {
        const response = await fetch(`/api/comments/${postId}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-Token': LikeButton.getCsrfToken()
          },
          body: JSON.stringify({ content, parentCommentId: parentId || null })
        });
        
        if (response.ok) {
          this.form.reset();
          this.form.dataset.replyTo = '';
          window.location.reload();
        }
      } catch (error) {
        console.error('Comment error:', error);
      } finally {
        submitBtn.disabled = false;
      }
    },
    
    replyTo(commentId) {
      this.form.dataset.replyTo = commentId;
      const textarea = this.form.querySelector('textarea');
      textarea.focus();
      textarea.placeholder = 'Write a reply...';
    }
  };

  // ============================================
  // Pagination / Infinite Scroll
  // ============================================
  const InfiniteScroll = {
    init() {
      const container = document.querySelector('.post-list[data-infinite]');
      if (!container) return;
      
      this.container = container;
      this.page = 1;
      this.loading = false;
      
      this.observer = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting && !this.loading) {
          this.loadMore();
        }
      });
      
      this.observer.observe(this.getSentinel());
    },
    
    getSentinel() {
      let sentinel = document.querySelector('.infinite-sentinel');
      if (!sentinel) {
        sentinel = document.createElement('div');
        sentinel.className = 'infinite-sentinel';
        this.container.appendChild(sentinel);
      }
      return sentinel;
    },
    
    async loadMore() {
      this.loading = true;
      const endpoint = this.container.dataset.endpoint + '?page=' + (++this.page);
      
      try {
        const response = await fetch(endpoint);
        const html = await response.text();
        
        if (html.trim()) {
          const temp = document.createElement('div');
          temp.innerHTML = html;
          
          const posts = temp.querySelectorAll('.post-card');
          posts.forEach(post => {
            this.container.insertBefore(post, this.getSentinel());
          });
        } else {
          this.observer.disconnect();
        }
      } catch (error) {
        console.error('Load more error:', error);
      } finally {
        this.loading = false;
      }
    }
  };

  // ============================================
  // Search
  // ============================================
  const Search = {
    init() {
      this.input = document.querySelector('.search-input');
      this.results = document.querySelector('.search-results');
      
      if (!this.input) return;
      
      this.debounce = null;
      this.input.addEventListener('input', () => this.handleInput());
      document.addEventListener('click', e => {
        if (!this.input.contains(e.target) && !this.results?.contains(e.target)) {
          this.close();
        }
      });
    },
    
    handleInput() {
      clearTimeout(this.debounce);
      this.debounce = setTimeout(() => this.search(), 300);
    },
    
    async search() {
      const query = this.input.value.trim();
      
      if (query.length < 2) {
        this.close();
        return;
      }
      
      try {
        const response = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
        const results = await response.json();
        this.showResults(results);
      } catch (error) {
        console.error('Search error:', error);
      }
    },
    
    showResults(results) {
      if (!this.results) return;
      
      this.results.innerHTML = results.map(post => `
        <a href="/blog/${post.slug}" class="search-result">
          <img src="${post.featuredImageUrl || ''}" alt="" class="search-result-image">
          <div class="search-result-content">
            <div class="search-result-title">${post.title}</div>
            <div class="search-result-meta">${post.authorName} · ${post.readTimeMin} min read</div>
          </div>
        </a>
      `).join('');
      
      this.results.style.display = 'block';
    },
    
    close() {
      if (this.results) {
        this.results.style.display = 'none';
      }
    }
  };

  // ============================================
  // Initialize
  // ============================================
  document.addEventListener('DOMContentLoaded', () => {
    Theme.init();
    MobileNav.init();
    ReadingProgress.init();
    Skeletons.init();
    LikeButton.init();
    Toast.init();
    Forms.init();
    Comments.init();
    InfiniteScroll.init();
    Search.init();
  });

})();