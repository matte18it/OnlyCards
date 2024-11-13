import {Injectable, Renderer2, RendererFactory2} from '@angular/core';

@Injectable({
  providedIn: 'root'
})

export class ThemeService {
  private renderer: Renderer2;

  constructor(private rendererFactory: RendererFactory2) {
    this.renderer = this.rendererFactory.createRenderer(null, null);
    this.applyStoredTheme();
  }

  private applyStoredTheme() {
    const storedTheme = this.loadThemeFromStorage();
    if (storedTheme) {
      this.applyTheme(storedTheme);
    }
    else {
      this.applyTheme('dark');
    }
  }
  public setFavicon(isDarkTheme: boolean) {
    const favicon = document.getElementById('app-favicon');
    if (favicon) {
      const iconPath = isDarkTheme ? 'faviconDark.ico' : 'faviconLight.ico';
      this.renderer.setAttribute(favicon, 'href', iconPath);
    }
  }
  public applyTheme(theme: string) {
    const root = document.documentElement;
    root.setAttribute('data-theme', theme);
    this.saveThemeToStorage(theme);
  }
  private saveThemeToStorage(theme: string) {
    localStorage.setItem('theme', theme);
  }
  private loadThemeFromStorage(): string | null {
    return localStorage.getItem('theme');
  }
 
}
