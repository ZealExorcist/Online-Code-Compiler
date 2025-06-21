# Logo Integration Guide

## Where to Add Your Logo

### 1. Logo File Location
Place your logo file in the frontend public directory:
```
frontend/public/logo.png
```

### 2. Supported Formats
- PNG (recommended for transparency)
- SVG (scalable vector graphics)
- JPG/JPEG (if no transparency needed)

### 3. Recommended Dimensions
- Width: 60-90px
- Height: 60-90px
- For best results, use a square or slightly rectangular logo
- Current size is set to 60px (increased by 50% from original 40px)

### 4. Current Configuration
The logo is already configured in the Header component:

**File:** `frontend/src/components/Header.vue`

**Line 6:** 
```vue
<img v-if="logoUrl" :src="logoUrl" alt="Logo" class="logo-image" />
```

**Line 52:**
```javascript
logoUrl: '/logo.png' // You can add your logo here
```

### 5. How to Change the Logo

#### Option 1: Replace the default file
1. Add your logo file to `frontend/public/logo.png`
2. The app will automatically use it

#### Option 2: Use a different filename
1. Add your logo file to `frontend/public/` (e.g., `my-company-logo.svg`)
2. Update the `logoUrl` in `Header.vue`:
```javascript
logoUrl: '/my-company-logo.svg'
```

#### Option 3: Use an external URL
```javascript
logoUrl: 'https://your-domain.com/logo.png'
```

### 6. Logo Styling
The logo styling is defined in the CSS:

```css
.logo-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
}
```

You can adjust these styles in the Header.vue file around line 119.

### 7. Favicon
To also update the browser tab icon:
1. Replace `frontend/public/favicon.ico` with your favicon
2. Or update the `<link rel="icon">` tag in `frontend/index.html`

### 8. Mobile Responsiveness
The logo automatically adjusts for mobile devices. You can customize mobile behavior in the `@media (max-width: 768px)` section of the Header.vue CSS.

### Example Logo Integration

1. Save your logo as `frontend/public/company-logo.png`
2. Update `Header.vue`:
```javascript
data() {
  return {
    languageCount: 10,
    showUserMenu: false,
    logoUrl: '/company-logo.png'
  }
}
```

That's it! Your logo will now appear in the header next to the "Online Compiler" text.
