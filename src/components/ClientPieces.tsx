import { ArrowUpRight, ChevronLeft, ChevronRight, Pause, Play, SkipBack, SkipForward } from 'lucide-react';
import type { HudOptions, Track } from '../clientData';

export function BrandMark({ small = false }: { small?: boolean }) {
  return (
    <span className={`brand-mark${small ? ' brand-mark-small' : ''}`} aria-hidden="true">
      <svg viewBox="0 0 32 32" fill="none">
        <path d="M4 18c0-7.7 6.3-14 14-14 1.4 0 2.7.2 4 .6C16.2 7.2 11 13.8 11 22c0 1.8.3 3.5.9 5.1C7.4 25.3 4 22 4 18Z" fill="#2f8f4e" />
        <path d="M11.9 27.1C13.5 29.5 16.1 31 19 31c6.1 0 11-4.9 11-11 0-8.2-5.2-14.8-12-17.4C21.7 6.2 25 11.6 25 18c0 5.5-4 10.1-9.3 10.9-.2.7-.5 1.4-.8 2.2Z" fill="#3db85c" />
        <path d="M12.4 22.4c0-6.6 4.2-12.2 10-14.6C20.9 6.2 19.5 6 18 6 10.3 6 4 12.3 4 18c0 4 2.7 7.4 6.5 8.9.6-1.4 1.1-2.9 1.5-4.5.3 0 .4 0 .4 0Z" fill="#ff6b8a" />
        <path d="M19 31c-2.9 0-5.5-1.5-7.1-3.9 2.8-1.1 5-3.4 6.2-6.3 2.6-6.4.8-13.4-4.3-17.2C16.3 3.2 18.6 3 21 3c7.2 0 13 5.8 13 13 0 8.3-6.7 15-15 15Z" fill="#ff8aa3" opacity=".35" />
        <circle cx="15.2" cy="16.2" r="1.05" fill="#1b1416" />
        <circle cx="19.6" cy="20.4" r=".9" fill="#1b1416" />
        <circle cx="13.4" cy="21.8" r=".7" fill="#1b1416" />
        <circle cx="20.8" cy="14.6" r=".7" fill="#1b1416" />
      </svg>
    </span>
  );
}

export function Switch({ checked, onChange, label, small = false }: { checked: boolean; onChange: () => void; label: string; small?: boolean }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      className={`switch${checked ? ' is-on' : ''}${small ? ' switch-small' : ''}`}
      onClick={onChange}
    >
      <span />
    </button>
  );
}

export function SpotifyGlyph({ size = 15 }: { size?: number }) {
  return (
    <svg aria-label="Spotify" role="img" width={size} height={size} viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="11" fill="#72e6ac" />
      <path d="M5.9 9.1c4-1.2 8.7-.8 12.2 1.2M6.8 12.1c3.3-1 7.4-.6 10.3 1.1M7.8 15.1c2.8-.7 5.8-.4 8.2 1" stroke="#101713" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  );
}

export function formatTime(value: number) {
  const seconds = Math.max(0, Math.floor(value));
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, '0')}`;
}

export function SpotifyWidget({ track, position, options, playing }: { track: Track; position: number; options: HudOptions; playing: boolean }) {
  const progress = Math.min(100, Math.max(0, (position / track.duration) * 100));
  return (
    <div className={`spotify-widget${options.glass ? ' is-glass' : ' is-solid'}${!options.artwork ? ' no-art' : ''}`}>
      {options.artwork && <img className="spotify-art" src="/images/album-art.jpg" alt="Fictional demo album artwork" draggable="false" />}
      <div className="spotify-widget-content">
        <div className="spotify-widget-topline">
          <span className="spotify-overline">{playing ? 'NOW PLAYING' : 'PAUSED'}</span>
          <SpotifyGlyph size={13} />
        </div>
        <div className="spotify-track-name">{track.title}</div>
        <div className="spotify-artist">{track.artist}</div>
        {options.progress && (
          <div className="spotify-widget-progress" aria-label={`${formatTime(position)} of ${formatTime(track.duration)}`}>
            <span style={{ width: `${progress}%` }} />
          </div>
        )}
      </div>
    </div>
  );
}

type PreviewPanelProps = {
  enabled: boolean;
  toggleEnabled: () => void;
  options: HudOptions;
  toggleOption: (option: keyof HudOptions) => void;
  track: Track;
  position: number;
  playing: boolean;
  setPlaying: (playing: boolean) => void;
  seek: (position: number) => void;
  changeTrack: (direction: number) => void;
  openEditor: () => void;
};

export function PreviewPanel({ enabled, toggleEnabled, options, toggleOption, track, position, playing, setPlaying, seek, changeTrack, openEditor }: PreviewPanelProps) {
  return (
    <aside className="preview-panel" aria-label="Spotify HUD preview and settings">
      <div className="preview-heading">
        <div>
          <h2>Spotify HUD</h2>
          <button className="text-link" onClick={openEditor}>Open editor <ArrowUpRight size={12} /></button>
        </div>
        <Switch checked={enabled} onChange={toggleEnabled} label="Enable Spotify HUD preview" />
      </div>

      <div className="preview-scene">
        <div className="preview-scene-label"><span /> IN-GAME</div>
        <div className="scene-crosshair" aria-hidden="true" />
        {enabled ? (
          <SpotifyWidget track={track} position={position} options={options} playing={playing} />
        ) : (
          <div className="preview-disabled">Spotify HUD is off</div>
        )}

      </div>

      <div className="playback-controls">
        <button type="button" aria-label="Previous demo track" onClick={() => changeTrack(-1)}><SkipBack size={15} fill="currentColor" /></button>
        <button type="button" className="play-button" aria-label={playing ? 'Pause demo playback' : 'Play demo playback'} onClick={() => setPlaying(!playing)}>
          {playing ? <Pause size={14} fill="currentColor" /> : <Play size={14} fill="currentColor" />}
        </button>
        <button type="button" aria-label="Next demo track" onClick={() => changeTrack(1)}><SkipForward size={15} fill="currentColor" /></button>
        <div className="playback-seek">
          <input aria-label="Seek demo track" type="range" min="0" max={track.duration} value={Math.min(position, track.duration)} onChange={(event) => seek(Number(event.target.value))} style={{ '--range-progress': `${(position / track.duration) * 100}%` } as React.CSSProperties} />
        </div>
        <span className="playback-time">{formatTime(position)}</span>
      </div>

      <div className="aside-divider" />
      <div className="hud-option-row">
        <div><span>Album artwork</span></div>
        <Switch checked={options.artwork} onChange={() => toggleOption('artwork')} label="Show album artwork" small />
      </div>
      <div className="hud-option-row">
        <div><span>Progress bar</span></div>
        <Switch checked={options.progress} onChange={() => toggleOption('progress')} label="Show track progress" small />
      </div>
      <div className="hud-option-row">
        <div><span>Glass background</span></div>
        <Switch checked={options.glass} onChange={() => toggleOption('glass')} label="Use glass background" small />
      </div>
      <button type="button" className="editor-link" onClick={openEditor}>Fine-tune your HUD <ChevronRight size={14} /></button>
    </aside>
  );
}

export function PageArrows({ onPrevious, onNext }: { onPrevious: () => void; onNext: () => void }) {
  return (
    <div className="page-arrows">
      <button onClick={onPrevious} aria-label="Previous item"><ChevronLeft size={15} /></button>
      <button onClick={onNext} aria-label="Next item"><ChevronRight size={15} /></button>
    </div>
  );
}