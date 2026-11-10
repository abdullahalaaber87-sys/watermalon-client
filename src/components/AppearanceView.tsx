import { Check, RotateCcw } from 'lucide-react';
import type { CSSProperties } from 'react';
import { accents, type AccentName } from '../clientData';
import { Switch } from './ClientPieces';

type AppearanceViewProps = {
  accent: AccentName;
  setAccent: (accent: AccentName) => void;
  opacity: number;
  setOpacity: (opacity: number) => void;
  radius: number;
  setRadius: (radius: number) => void;
  uiScale: number;
  setUiScale: (uiScale: number) => void;
  blur: boolean;
  setBlur: (blur: boolean) => void;
  resetWindow: () => void;
  reset: () => void;
};

const progress = (value: number, min: number, max: number) => ({ '--range-progress': `${((value - min) / (max - min)) * 100}%` } as CSSProperties);

export function AppearanceView({ accent, setAccent, opacity, setOpacity, radius, setRadius, uiScale, setUiScale, blur, setBlur, resetWindow, reset }: AppearanceViewProps) {
  return (
    <section className="workspace-content appearance-workspace">
      <div className="page-heading editor-heading">
        <div><h1>Set the <em>tone.</em></h1></div>
        <button className="subtle-button" onClick={reset}><RotateCcw size={14} /> Reset all</button>
      </div>

      <div className="appearance-section">
        <div className="setting-title"><div><h3>Window</h3></div><button className="text-link" onClick={resetWindow}>Reset size &amp; position</button></div>
        <label className="range-setting"><span><strong>GUI size</strong></span><b>{uiScale}%</b>
          <input type="range" min="62" max="110" value={uiScale} aria-label="GUI size" onChange={(event) => setUiScale(Number(event.target.value))} style={progress(uiScale, 62, 110)} />
        </label>
        <div className="size-presets" role="group" aria-label="GUI size presets">
          {[{ label: 'Small', value: 70 }, { label: 'Normal', value: 82 }, { label: 'Large', value: 96 }, { label: 'Huge', value: 110 }].map((preset) => (
            <button key={preset.label} className={uiScale === preset.value ? 'active' : ''} onClick={() => setUiScale(preset.value)}>{preset.label}</button>
          ))}
        </div>
        <label className="range-setting"><span><strong>Opacity</strong></span><b>{opacity}%</b>
          <input type="range" min="70" max="100" value={opacity} aria-label="Window opacity" onChange={(event) => setOpacity(Number(event.target.value))} style={progress(opacity, 70, 100)} />
        </label>
        <label className="range-setting"><span><strong>Corner radius</strong></span><b>{radius}px</b>
          <input type="range" min="0" max="20" value={radius} aria-label="Corner radius" onChange={(event) => setRadius(Number(event.target.value))} style={progress(radius, 0, 20)} />
        </label>
        <div className="toggle-setting"><span><strong>Blur the world behind the menu</strong></span><Switch checked={blur} onChange={() => setBlur(!blur)} label="Blur the world behind the menu" small /></div>
      </div>

      <div className="appearance-section surface-settings">
        <div className="setting-title"><div><h3>Accent color</h3></div></div>
        <div className="accent-options">
          {accents.map((item) => (
            <button type="button" key={item.id} className={`accent-option${accent === item.id ? ' selected' : ''}`} onClick={() => setAccent(item.id)} aria-pressed={accent === item.id}>
              <span className="accent-swatch" style={{ background: item.color }}>{accent === item.id && <Check size={15} strokeWidth={2.5} />}</span>
              <span><strong>{item.label}</strong><small>{item.description}</small></span>
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}
