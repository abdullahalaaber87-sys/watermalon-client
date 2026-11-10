import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState, type CSSProperties, type Dispatch, type PointerEvent as ReactPointerEvent, type SetStateAction } from 'react';
import { ArrowUpRight, ChevronDown, CircleHelp, Code2, Command, LayoutPanelTop, Minus, Plus, RotateCcw, Search, SlidersHorizontal, Star, X } from 'lucide-react';
import {
  accents, categories, defaultEnabled, defaultFavorites, defaultHudLayout, defaultHudOptions, defaultKeybinds, defaultSettings, modules, tracks,
  type AccentName, type Category, type Filter, type HudElementId, type HudLayout, type HudOptions, type HudPoint, type Mode, type ModuleSetting, type View, type WindowRect,
} from './clientData';
import { AppearanceView } from './components/AppearanceView';
import { BrandMark, PreviewPanel, Switch } from './components/ClientPieces';
import { GameHud, type HudKeys, type HudStats } from './components/GameHud';
import { HudToolbar } from './components/HudToolbar';

function useStoredState<T>(key: string, initial: T): [T, Dispatch<SetStateAction<T>>] {
  const [value, setValue] = useState<T>(() => {
    try {
      const saved = localStorage.getItem(`watermalon:${key}`);
      if (!saved) return initial;
      const parsed = JSON.parse(saved) as T;
      return typeof initial === 'object' && initial !== null && !Array.isArray(initial) && parsed !== null
        ? { ...initial, ...parsed }
        : parsed;
    } catch {
      return initial;
    }
  });
  useLayoutEffect(() => {
    try { localStorage.setItem(`watermalon:${key}`, JSON.stringify(value)); } catch { /* Works without storage. */ }
  }, [key, value]);
  return [value, setValue];
}

function useViewport() {
  const [size, setSize] = useState(() => ({ w: window.innerWidth, h: window.innerHeight }));
  useEffect(() => {
    const onResize = () => setSize({ w: window.innerWidth, h: window.innerHeight });
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);
  return size;
}

const MIN_W = 600;
const MIN_H = 380;
const MARGIN = 8;

function clampRect(rect: WindowRect, scale: number, vw: number, vh: number): WindowRect {
  const w = Math.max(MIN_W, Math.min(rect.w, (vw - MARGIN * 2) / scale));
  const h = Math.max(MIN_H, Math.min(rect.h, (vh - MARGIN * 2) / scale));
  const maxX = Math.max(0, (vw - w * scale) / 2 - MARGIN);
  const maxY = Math.max(0, (vh - h * scale) / 2 - MARGIN);
  return { w, h, x: Math.max(-maxX, Math.min(maxX, rect.x)), y: Math.max(-maxY, Math.min(maxY, rect.y)) };
}

function ImplementationModal({ close }: { close: () => void }) {
  return (
    <div className="modal-backdrop" onMouseDown={close}>
      <section className="implementation-modal" role="dialog" aria-modal="true" aria-labelledby="implementation-title" onMouseDown={(event) => event.stopPropagation()}>
        <div className="modal-top"><span className="section-kicker">WATERMALON  /  1.21.11</span><button className="icon-button" onClick={close} aria-label="Close implementation notes"><X size={17} /></button></div>
        <h2 id="implementation-title">Build the real mod.</h2>
        <p className="modal-intro">A Fabric 1.21.11 mod with this click GUI, the draggable HUD, and Spotify. It's set up to build on GitHub Actions.</p>
        <div className="findings-list">
          <div><span>01</span><p><strong>Upload to GitHub</strong><small>Create a repository, then upload everything inside <code>watermalon-mod.zip</code>, including the hidden <code>.github</code> folder.</small></p></div>
          <div><span>02</span><p><strong>Let Actions build it</strong><small>Open the Actions tab. After the green check (about 3–5 minutes), download <code>watermalon-jar</code> from Artifacts.</small></p></div>
          <div><span>03</span><p><strong>Install</strong><small>Put the jar and Fabric API in <code>.minecraft/mods</code>, start Fabric 1.21.11, and press Right Shift.</small></p></div>
        </div>
        <div className="modal-notice"><CircleHelp size={16} /><span>Original code: 8 quality-of-life modules plus the GUI and HUD. Spotify needs your own free Client ID (see the README).</span></div>
        <div className="modal-actions"><a className="primary-button" href="/watermalon-mod.zip" download>Download mod source <ArrowUpRight size={15} /></a><a className="download-link" href="/watermalon-1.0.0.jar" download>Download built jar</a></div>
      </section>
    </div>
  );
}

const measureCanvas = typeof document !== 'undefined' ? document.createElement('canvas').getContext('2d') : null;
function textWidth(text: string) {
  if (!measureCanvas) return text.length * 7;
  measureCanvas.font = '600 12px Inter, Arial, sans-serif';
  return measureCanvas.measureText(text).width;
}

const noKeys: HudKeys = { w: false, a: false, s: false, d: false, space: false, lmb: false, rmb: false, lcps: 0, rcps: 0 };

function App() {
  const [mode, setMode] = useState<Mode>('gui');
  const [view, setView] = useState<View>('modules');
  const [category, setCategory] = useState<Category>('All modules');
  const [filter, setFilter] = useState<Filter>('all');
  const [query, setQuery] = useState('');
  const [expanded, setExpanded] = useState<string | null>(null);
  const [listening, setListening] = useState<string | null>(null);
  const [showNotes, setShowNotes] = useState(false);
  const [enabled, setEnabled] = useStoredState('enabled', defaultEnabled);
  const [favorites, setFavorites] = useStoredState('favorites', defaultFavorites);
  const [keybinds, setKeybinds] = useStoredState('keybinds', defaultKeybinds);
  const [moduleSettings, setModuleSettings] = useStoredState<Record<string, ModuleSetting[]>>('settings', defaultSettings);
  const [options, setOptions] = useStoredState<HudOptions>('hud-options', defaultHudOptions);
  const [hudLayout, setHudLayout] = useStoredState<HudLayout>('hud-layout', defaultHudLayout);
  const [hudScale, setHudScale] = useStoredState('hud-scale', 100);
  const [accent, setAccent] = useStoredState<AccentName>('accent', 'melon');
  const [opacity, setOpacity] = useStoredState('opacity', 96);
  const [radius, setRadius] = useStoredState('radius', 12);
  const [uiScale, setUiScale] = useStoredState('ui-scale', 82);
  const [windowRect, setWindowRect] = useStoredState<WindowRect | null>('window', null);
  const [trackIndex, setTrackIndex] = useState(0);
  const [trackPosition, setTrackPosition] = useState(84);
  const [playing, setPlaying] = useState(true);
  const [stats, setStats] = useState<HudStats>({ fps: 144, ping: 24, x: 128, y: 64, z: -247, facing: 'N' });
  const [keys, setKeys] = useState<HudKeys>(noKeys);
  const [hotbarSlot, setHotbarSlot] = useState(0);
  const [zooming, setZooming] = useState(false);
  const searchRef = useRef<HTMLInputElement>(null);
  const clicks = useRef<{ l: number[]; r: number[] }>({ l: [], r: [] });
  const gesture = useRef<{ kind: 'move' | 'resize'; axes: string; px: number; py: number; start: WindowRect } | null>(null);
  const viewport = useViewport();
  const isDesktop = viewport.w > 780;
  const track = tracks[trackIndex];
  const currentAccent = accents.find((item) => item.id === accent) ?? accents[0];
  const scale = uiScale / 100;
  const open = mode === 'gui';

  const settingValue = useCallback((moduleId: string, settingId: string) => (moduleSettings[moduleId] ?? defaultSettings[moduleId] ?? []).find((setting) => setting.id === settingId)?.value, [moduleSettings]);
  const blurOn = Boolean(settingValue('clickgui', 'blur') ?? true);

  const defaultRect: WindowRect = { x: 0, y: 0, w: Math.round(Math.min(880, viewport.w * 0.78)), h: Math.round(Math.min(548, viewport.h * 0.74)) };
  const frame = clampRect(windowRect ?? defaultRect, scale, viewport.w, viewport.h);

  // Demo playback clock.
  useEffect(() => {
    if (!playing) return;
    const timer = window.setInterval(() => setTrackPosition((position) => Math.min(track.duration, position + 1)), 1000);
    return () => window.clearInterval(timer);
  }, [playing, track.duration]);

  useEffect(() => {
    if (trackPosition >= track.duration && playing) {
      setTrackIndex((index) => (index + 1) % tracks.length);
      setTrackPosition(0);
    }
  }, [trackPosition, track.duration, playing]);

  // Real browser frame rate for the FPS readouts.
  useEffect(() => {
    let frames = 0;
    let last = performance.now();
    let raf = 0;
    const loop = (now: number) => {
      frames += 1;
      if (now - last >= 500) {
        const fps = Math.round((frames * 1000) / (now - last));
        setStats((previous) => (previous.fps === fps ? previous : { ...previous, fps }));
        frames = 0;
        last = now;
      }
      raf = requestAnimationFrame(loop);
    };
    raf = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(raf);
  }, []);

  // Movement: WASD (or AutoWalk) moves the coordinates while in game.
  const autoWalk = Boolean(enabled.autowalk);
  useEffect(() => {
    if (mode !== 'hud') return;
    const timer = window.setInterval(() => {
      setKeys((current) => {
        const forward = current.w || autoWalk;
        const dz = (current.s ? 1 : 0) - (forward ? 1 : 0);
        const dx = (current.d ? 1 : 0) - (current.a ? 1 : 0);
        if (dx || dz) {
          setStats((previous) => ({ ...previous, x: previous.x + dx * 0.22, z: previous.z + dz * 0.22, facing: dz < 0 ? 'N' : dz > 0 ? 'S' : dx > 0 ? 'E' : 'W' }));
        }
        return current;
      });
      const now = performance.now();
      clicks.current.l = clicks.current.l.filter((time) => now - time < 1000);
      clicks.current.r = clicks.current.r.filter((time) => now - time < 1000);
      setKeys((current) => (current.lcps === clicks.current.l.length && current.rcps === clicks.current.r.length ? current : { ...current, lcps: clicks.current.l.length, rcps: clicks.current.r.length }));
    }, 50);
    return () => window.clearInterval(timer);
  }, [mode, autoWalk]);

  useEffect(() => {
    if (mode !== 'hud') {
      setKeys(noKeys);
      setZooming(false);
    }
  }, [mode]);

  // Keyboard: menu toggle, binds, and in-game inputs.
  useEffect(() => {
    function setKey(event: KeyboardEvent, down: boolean) {
      const map: Record<string, keyof HudKeys> = { KeyW: 'w', KeyA: 'a', KeyS: 's', KeyD: 'd', Space: 'space' };
      const key = map[event.code];
      if (key) {
        setKeys((current) => (current[key] === down ? current : { ...current, [key]: down }));
        return true;
      }
      return false;
    }

    function onKeyDown(event: KeyboardEvent) {
      if (listening) {
        event.preventDefault();
        if (event.key === 'Escape') { setListening(null); return; }
        if (['Shift', 'Control', 'Alt', 'Meta'].includes(event.key)) return;
        const key = event.key === 'Backspace' || event.key === 'Delete' ? '' : event.key === ' ' ? 'SPACE' : event.key.toUpperCase();
        setKeybinds((previous) => ({ ...previous, [listening]: key }));
        setListening(null);
        return;
      }
      if (event.key === 'Escape') {
        if (showNotes) setShowNotes(false);
        else if (mode === 'edit') setMode('gui');
        else if (mode === 'gui') setMode('hud');
        return;
      }
      if (event.code === 'ShiftRight' && !event.repeat) {
        setMode((current) => (current === 'gui' ? 'hud' : 'gui'));
        return;
      }
      const target = event.target as HTMLElement | null;
      if (target?.matches('input, textarea, select') || target?.isContentEditable) return;
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        setMode('gui');
        setView('modules');
        window.setTimeout(() => searchRef.current?.focus(), 0);
        return;
      }
      if (mode === 'edit' || showNotes || event.ctrlKey || event.metaKey || event.altKey) return;
      const key = event.key === ' ' ? 'SPACE' : event.key.toUpperCase();

      if (mode === 'hud') {
        if (setKey(event, true)) event.preventDefault();
        if (/^[1-9]$/.test(event.key)) setHotbarSlot(Number(event.key) - 1);
        if (enabled.zoom && keybinds.zoom && keybinds.zoom === key) {
          setZooming(true);
          return;
        }
      }
      if (event.repeat) return;
      const matched = modules.filter((module) => module.id !== 'zoom' && keybinds[module.id] && keybinds[module.id] === key);
      if (matched.length) {
        event.preventDefault();
        setEnabled((previous) => {
          const next = { ...previous };
          matched.forEach((module) => { next[module.id] = !next[module.id]; });
          return next;
        });
      }
    }

    function onKeyUp(event: KeyboardEvent) {
      if (mode !== 'hud') return;
      setKey(event, false);
      const key = event.key === ' ' ? 'SPACE' : event.key.toUpperCase();
      if (keybinds.zoom && keybinds.zoom === key) setZooming(false);
    }

    const clear = () => { setKeys(noKeys); setZooming(false); };
    window.addEventListener('keydown', onKeyDown);
    window.addEventListener('keyup', onKeyUp);
    window.addEventListener('blur', clear);
    return () => {
      window.removeEventListener('keydown', onKeyDown);
      window.removeEventListener('keyup', onKeyUp);
      window.removeEventListener('blur', clear);
    };
  }, [enabled.zoom, keybinds, listening, mode, setEnabled, setKeybinds, showNotes]);

  function toggleEnabled(id: string) {
    setEnabled((previous) => ({ ...previous, [id]: !previous[id] }));
  }

  function toggleOption(option: keyof HudOptions) {
    setOptions((previous) => ({ ...previous, [option]: !previous[option] }));
  }

  function updateSetting(moduleId: string, settingId: string, value: boolean | number | string) {
    setModuleSettings((previous) => ({
      ...previous,
      [moduleId]: (previous[moduleId] ?? defaultSettings[moduleId] ?? []).map((setting) => setting.id === settingId ? { ...setting, value } as ModuleSetting : setting),
    }));
  }

  function changeTrack(direction: number) {
    setTrackIndex((index) => (index + direction + tracks.length) % tracks.length);
    setTrackPosition(0);
    setPlaying(true);
  }

  function selectCategory(next: Category) {
    setCategory(next);
    setFilter('all');
    setView('modules');
    setExpanded(null);
  }

  function setPoint(id: HudElementId, point: HudPoint) {
    setHudLayout((previous) => ({ ...previous, [id]: point }));
  }

  // Window move and resize. Coordinates are screen pixels; size is unscaled.
  function startGesture(event: ReactPointerEvent<HTMLElement>, kind: 'move' | 'resize', axes = '') {
    if (!isDesktop || event.button !== 0) return;
    if (kind === 'move' && (event.target as HTMLElement).closest('button, input, a, label')) return;
    event.preventDefault();
    event.currentTarget.setPointerCapture(event.pointerId);
    gesture.current = { kind, axes, px: event.clientX, py: event.clientY, start: frame };
  }

  function moveGesture(event: ReactPointerEvent<HTMLElement>) {
    const current = gesture.current;
    if (!current) return;
    const dx = event.clientX - current.px;
    const dy = event.clientY - current.py;
    const { start } = current;
    if (current.kind === 'move') {
      setWindowRect(clampRect({ ...start, x: start.x + dx, y: start.y + dy }, scale, viewport.w, viewport.h));
      return;
    }
    const next = { ...start };
    if (current.axes.includes('e')) {
      next.w = start.w + dx / scale;
      next.x = start.x + ((next.w - start.w) * scale) / 2;
    }
    if (current.axes.includes('s')) {
      next.h = start.h + dy / scale;
      next.y = start.y + ((next.h - start.h) * scale) / 2;
    }
    const clamped = clampRect(next, scale, viewport.w, viewport.h);
    // Keep the top-left edge pinned when a size limit is hit.
    clamped.x = start.x + ((clamped.w - start.w) * scale) / 2;
    clamped.y = start.y + ((clamped.h - start.h) * scale) / 2;
    setWindowRect(clampRect(clamped, scale, viewport.w, viewport.h));
  }

  function endGesture() {
    gesture.current = null;
  }

  function resetWindow() {
    setWindowRect(null);
    setUiScale(82);
  }

  function stepScale(delta: number) {
    setUiScale((current) => Math.max(62, Math.min(110, current + delta)));
  }

  // In-game mouse buttons feed the keystrokes CPS counter.
  function onGamePointerDown(event: ReactPointerEvent<HTMLDivElement>) {
    if (mode !== 'hud') return;
    if ((event.target as HTMLElement).closest('button, a, input')) return;
    const side = event.button === 2 ? 'r' : event.button === 0 ? 'l' : null;
    if (!side) return;
    clicks.current[side].push(performance.now());
    setKeys((current) => ({ ...current, [side === 'l' ? 'lmb' : 'rmb']: true, [side === 'l' ? 'lcps' : 'rcps']: clicks.current[side].length }));
  }

  function onGamePointerUp(event: ReactPointerEvent<HTMLDivElement>) {
    if (mode !== 'hud') return;
    if (event.button === 0) setKeys((current) => ({ ...current, lmb: false }));
    if (event.button === 2) setKeys((current) => ({ ...current, rmb: false }));
  }

  const visibleModules = modules.filter((module) => {
    const matchesCategory = category === 'All modules' || module.category === category;
    const matchesFilter = filter === 'all' || (filter === 'enabled' ? enabled[module.id] : favorites[module.id]);
    const search = query.trim().toLowerCase();
    return matchesCategory && matchesFilter && (!search || `${module.name} ${module.description} ${module.category}`.toLowerCase().includes(search));
  });
  const enabledCount = modules.filter((module) => enabled[module.id]).length;
  const favoriteCount = modules.filter((module) => favorites[module.id]).length;
  const arrayNames = useMemo(() => modules
    .filter((module) => enabled[module.id] && module.category !== 'Client')
    .map((module) => module.name)
    .sort((a, b) => textWidth(b) - textWidth(a)), [enabled]);

  const hudOn = Boolean(enabled.hud);
  const shown: Record<HudElementId, boolean> = {
    watermark: options.watermark,
    arraylist: options.moduleList,
    spotify: Boolean(enabled.spotify),
    info: options.info,
    keystrokes: options.keystrokes,
  };
  const hudVisible: Record<HudElementId, boolean> = {
    watermark: hudOn && shown.watermark,
    arraylist: hudOn && shown.arraylist,
    spotify: shown.spotify,
    info: hudOn && shown.info,
    keystrokes: hudOn && shown.keystrokes,
  };

  function toggleHudElement(id: HudElementId) {
    if (id === 'spotify') { toggleEnabled('spotify'); return; }
    const option: Record<Exclude<HudElementId, 'spotify'>, keyof HudOptions> = { watermark: 'watermark', arraylist: 'moduleList', info: 'info', keystrokes: 'keystrokes' };
    toggleOption(option[id]);
  }

  const zoomAmount = Number(settingValue('zoom', 'amount') ?? 4);
  const crosshair = { enabled: Boolean(enabled.crosshair), style: String(settingValue('crosshair', 'style') ?? 'Plus'), size: Number(settingValue('crosshair', 'size') ?? 8) };

  const appStyle = { '--accent': currentAccent.color, '--accent-rgb': currentAccent.rgb, '--surface-opacity': opacity / 100, '--window-radius': `${radius}px` } as CSSProperties;
  const worldClass = ['world-background', enabled.fullbright ? 'is-fullbright' : '', open && blurOn ? 'is-blurred' : '', zooming ? 'is-zoomed' : ''].filter(Boolean).join(' ');
  const frameStyle = isDesktop ? { width: frame.w, height: frame.h, transform: `translate(${frame.x}px, ${frame.y}px) scale(${scale})` } as CSSProperties : undefined;

  return (
    <div
      className={`game-shell mode-${mode}`}
      style={appStyle}
      onPointerDown={onGamePointerDown}
      onPointerUp={onGamePointerUp}
      onContextMenu={(event) => { if (mode === 'hud') event.preventDefault(); }}
      onWheel={(event) => { if (mode === 'hud') setHotbarSlot((slot) => (slot + (event.deltaY > 0 ? 1 : 8)) % 9); }}
    >
      <div className={worldClass} style={{ '--zoom': 1 + (zoomAmount - 1) * 0.25 } as CSSProperties} />
      <div className={`world-vignette${open ? ' is-menu' : ''}`} />

      {mode !== 'gui' && (
        <GameHud
          layout={hudLayout}
          setPoint={setPoint}
          editing={mode === 'edit'}
          visible={mode === 'edit' ? { ...hudVisible, ...Object.fromEntries(Object.entries(shown).map(([id, value]) => [id, value && (hudOn || id === 'spotify')])) } : hudVisible}
          scale={hudScale / 100}
          options={options}
          arrayNames={arrayNames}
          track={track}
          trackPosition={trackPosition}
          playing={playing}
          stats={stats}
          keys={keys}
          crosshair={crosshair}
          hotbarSlot={hotbarSlot}
        />
      )}

      {mode === 'edit' && (
        <HudToolbar
          shown={shown}
          toggle={toggleHudElement}
          scale={hudScale}
          setScale={setHudScale}
          hudEnabled={hudOn}
          enableHud={() => setEnabled((previous) => ({ ...previous, hud: true }))}
          reset={() => { setHudLayout(defaultHudLayout); setHudScale(100); }}
          done={() => setMode('gui')}
        />
      )}

      {mode === 'hud' && (
        <div className="hud-hint">
          <button onClick={() => setMode('gui')}><kbd>R SHIFT</kbd> Menu</button>
          <button onClick={() => setMode('edit')}><LayoutPanelTop size={12} /> Edit HUD</button>
        </div>
      )}

      {open && (
        <div className="window-frame" style={frameStyle}>
          <main className="client-window" aria-label="Watermalon click GUI">
            <header
              className={`window-header${isDesktop ? ' is-draggable' : ''}`}
              onPointerDown={(event) => startGesture(event, 'move')}
              onPointerMove={moveGesture}
              onPointerUp={endGesture}
              onPointerCancel={endGesture}
              onDoubleClick={(event) => { if (!(event.target as HTMLElement).closest('button')) setWindowRect((current) => current ? { ...current, x: 0, y: 0 } : null); }}
            >
              <div className="brand-lockup"><BrandMark /><div><strong>WATER<span>MALON</span></strong><small>FRESH. SHARP. YOURS.</small></div></div>
              <nav className="top-navigation" aria-label="Client views">
                <button className={view === 'modules' ? 'active' : ''} onClick={() => setView('modules')}>Modules</button>
                <button onClick={() => setMode('edit')}>HUD Editor</button>
                <button className={view === 'appearance' ? 'active' : ''} onClick={() => setView('appearance')}>Appearance</button>
              </nav>
              <div className="header-actions">
                {isDesktop && (
                  <div className="size-stepper" role="group" aria-label="GUI size">
                    <button onClick={() => stepScale(-4)} aria-label="Make GUI smaller" disabled={uiScale <= 62}><Minus size={12} /></button>
                    <span>{uiScale}%</span>
                    <button onClick={() => stepScale(4)} aria-label="Make GUI bigger" disabled={uiScale >= 110}><Plus size={12} /></button>
                  </div>
                )}
                {isDesktop && <button className="notes-button" onClick={resetWindow} title="Reset window" aria-label="Reset window size and position"><RotateCcw size={13} /></button>}
                <button className="notes-button" onClick={() => setShowNotes(true)} title="Build notes" aria-label="Build notes"><Code2 size={15} /></button>
                <button className="close-button" onClick={() => setMode('hud')} aria-label="Close client preview"><X size={17} /></button>
              </div>
            </header>

            <div className="window-body">
              <aside className="sidebar">
                <div className="sidebar-nav">
                  {categories.map(({ label, icon: Icon }) => {
                    const count = label === 'All modules' ? modules.length : modules.filter((module) => module.category === label).length;
                    return <button key={label} className={view === 'modules' && category === label ? 'selected' : ''} onClick={() => selectCategory(label)}><Icon size={16} strokeWidth={1.7} /><span>{label}</span><small>{String(count).padStart(2, '0')}</small></button>;
                  })}
                </div>
                <div className="sidebar-rule" />
                <div className="sidebar-nav sidebar-tools">
                  <button onClick={() => setMode('edit')}><LayoutPanelTop size={16} strokeWidth={1.7} /><span>HUD editor</span><ArrowUpRight size={13} /></button>
                  <button className={view === 'appearance' ? 'selected' : ''} onClick={() => setView('appearance')}><SlidersHorizontal size={16} strokeWidth={1.7} /><span>Appearance</span><ArrowUpRight size={13} /></button>
                </div>
                <div className="sidebar-bottom"><div className="profile-avatar">W</div><div><strong>Watermalon</strong><small>{modules.length} modules loaded</small></div><ChevronDown size={14} /></div>
              </aside>

              {view === 'modules' && (
                <section className="workspace-content modules-workspace">
                  <div className="page-heading modules-heading">
                    <div><h1>Modules<span className="accent-dot">.</span></h1></div>
                    <label className="search-box"><Search size={16} /><input ref={searchRef} type="search" placeholder="Search modules..." value={query} onChange={(event) => setQuery(event.target.value)} aria-label="Search modules" /><kbd><Command size={10} /> K</kbd></label>
                  </div>
                  <div className="module-toolbar">
                    <div className="filter-tabs" role="group" aria-label="Filter modules">
                      <button className={filter === 'all' ? 'active' : ''} onClick={() => setFilter('all')}>All modules <span>{modules.length}</span></button>
                      <button className={filter === 'enabled' ? 'active' : ''} onClick={() => setFilter('enabled')}>Enabled <span>{enabledCount}</span></button>
                      <button className={filter === 'favorites' ? 'active' : ''} onClick={() => setFilter('favorites')}>Favorites <span>{favoriteCount}</span></button>
                    </div>
                    <span className="toolbar-sorting">{visibleModules.length} SHOWN</span>
                  </div>
                  <div className="module-list">
                    {visibleModules.length ? visibleModules.map((module, index) => {
                      const Icon = module.icon;
                      const isExpanded = expanded === module.id;
                      return <div className={`module-entry${isExpanded ? ' expanded' : ''}`} key={module.id} style={{ '--row-index': index } as CSSProperties}>
                        <div className="module-row">
                          <button type="button" className="module-main" onClick={() => setExpanded(isExpanded ? null : module.id)} aria-expanded={isExpanded} aria-label={`${module.name} settings`}>
                            <span className={`module-icon${enabled[module.id] ? ' enabled' : ''}`}><Icon size={18} strokeWidth={1.65} /></span>
                            <span className="module-copy"><strong>{module.name}</strong><small>{module.description}</small></span>
                          </button>
                          <button className={`favorite-button${favorites[module.id] ? ' is-favorite' : ''}`} onClick={() => setFavorites((previous) => ({ ...previous, [module.id]: !previous[module.id] }))} aria-label={`${favorites[module.id] ? 'Remove' : 'Add'} ${module.name} ${favorites[module.id] ? 'from' : 'to'} favorites`} title={favorites[module.id] ? 'Remove favorite' : 'Add favorite'}><Star size={14} fill={favorites[module.id] ? 'currentColor' : 'none'} /></button>
                          <span className="module-category">{module.category.toUpperCase()}</span>
                          <Switch checked={Boolean(enabled[module.id])} onChange={() => toggleEnabled(module.id)} label={`Toggle ${module.name}`} />
                        </div>
                        {isExpanded && <div className="module-expanded">
                          <div className="setting-row">
                            <div><strong>Keyboard shortcut</strong><small>{listening === module.id ? 'Press a key. Backspace clears; Esc cancels.' : module.id === 'zoom' ? 'Hold in game to zoom.' : 'Click to set a shortcut.'}</small></div>
                            <button className={`keybind-button${listening === module.id ? ' listening' : ''}`} onClick={() => setListening(module.id)}>{listening === module.id ? 'PRESS A KEY' : keybinds[module.id] || 'NONE'}</button>
                          </div>
                          {(moduleSettings[module.id] ?? module.settings).map((setting) => (
                            <div className="setting-row" key={setting.id}>
                              <div><strong>{setting.name}</strong><small>{setting.description}</small></div>
                              {setting.type === 'toggle' && <Switch checked={Boolean(setting.value)} onChange={() => updateSetting(module.id, setting.id, !setting.value)} label={`${setting.name} for ${module.name}`} small />}
                              {setting.type === 'slider' && (
                                <label className="inline-range">
                                  <b>{setting.value}</b>
                                  <input type="range" min={setting.min} max={setting.max} step={setting.step} value={setting.value} onChange={(event) => updateSetting(module.id, setting.id, Number(event.target.value))} style={{ '--range-progress': `${((Number(setting.value) - setting.min) / (setting.max - setting.min)) * 100}%` } as CSSProperties} />
                                </label>
                              )}
                              {setting.type === 'mode' && (
                                <div className="mode-pills">
                                  {setting.options.map((option) => (
                                    <button key={option} className={setting.value === option ? 'active' : ''} onClick={() => updateSetting(module.id, setting.id, option)}>{option}</button>
                                  ))}
                                </div>
                              )}
                            </div>
                          ))}
                          {(module.id === 'spotify' || module.id === 'hud') && <button className="mini-editor-link" onClick={() => setMode('edit')}>Edit HUD layout <ArrowUpRight size={12} /></button>}
                        </div>}
                      </div>;
                    }) : <div className="empty-state"><Search size={21} /><strong>No modules found</strong><p>Try another search or filter.</p><button onClick={() => { setQuery(''); setFilter('all'); setCategory('All modules'); }}>Clear filters</button></div>}
                  </div>
                  <div className="workspace-footer"><span><i /> {enabledCount} ENABLED</span></div>
                </section>
              )}

              {view === 'appearance' && (
                <AppearanceView
                  accent={accent} setAccent={setAccent}
                  opacity={opacity} setOpacity={setOpacity}
                  radius={radius} setRadius={setRadius}
                  uiScale={uiScale} setUiScale={setUiScale}
                  blur={blurOn} setBlur={(value) => updateSetting('clickgui', 'blur', value)}
                  resetWindow={resetWindow}
                  reset={() => { setAccent('melon'); setOpacity(96); setRadius(12); resetWindow(); }}
                />
              )}

              <PreviewPanel enabled={Boolean(enabled.spotify)} toggleEnabled={() => toggleEnabled('spotify')} options={options} toggleOption={toggleOption} track={track} position={trackPosition} playing={playing} setPlaying={setPlaying} seek={setTrackPosition} changeTrack={changeTrack} openEditor={() => setMode('edit')} />
            </div>
          </main>
          {isDesktop && (
            <>
              <div className="resize-handle resize-e" onPointerDown={(event) => startGesture(event, 'resize', 'e')} onPointerMove={moveGesture} onPointerUp={endGesture} onPointerCancel={endGesture} aria-hidden="true" />
              <div className="resize-handle resize-s" onPointerDown={(event) => startGesture(event, 'resize', 's')} onPointerMove={moveGesture} onPointerUp={endGesture} onPointerCancel={endGesture} aria-hidden="true" />
              <div className="resize-handle resize-se" onPointerDown={(event) => startGesture(event, 'resize', 'se')} onPointerMove={moveGesture} onPointerUp={endGesture} onPointerCancel={endGesture} title="Drag to resize" aria-label="Resize window" role="separator" />
            </>
          )}
        </div>
      )}

      {open && <div className="outer-footer"><span>{isDesktop ? 'Drag header to move · corner to resize' : ''}</span><span><kbd>R SHIFT</kbd> TO CLOSE</span></div>}
      {showNotes && <ImplementationModal close={() => setShowNotes(false)} />}
    </div>
  );
}

export default App;
