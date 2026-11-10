import { chromium } from 'playwright';
import assert from 'node:assert/strict';

const baseUrl = process.env.PREVIEW_URL || 'http://localhost:5173';
const browser = await chromium.launch({ headless: true, args: ['--no-sandbox'] });
const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
const errors = [];
page.on('pageerror', (error) => errors.push(error.message));
const accentOf = () => page.locator('.game-shell').evaluate((element) => getComputedStyle(element).getPropertyValue('--accent').trim());

try {
  await page.goto(baseUrl, { waitUntil: 'networkidle' });
  await page.evaluate(() => localStorage.clear());
  await page.reload({ waitUntil: 'networkidle' });

  // Modules, filters, search, persistence.
  assert.equal(await page.getByRole('heading', { name: 'Modules.' }).count(), 1);
  await page.getByRole('switch', { name: 'Toggle FullBright' }).click();
  await page.reload({ waitUntil: 'networkidle' });
  assert.equal(await page.getByRole('switch', { name: 'Toggle FullBright' }).getAttribute('aria-checked'), 'false');
  await page.getByRole('button', { name: /Movement 06/ }).click();
  assert.equal(await page.locator('.module-entry').count(), 6);
  await page.getByRole('button', { name: /All modules 58/ }).first().click();
  await page.getByRole('searchbox', { name: 'Search modules' }).fill('customcrosshair');
  assert.equal(await page.locator('.module-entry').count(), 1);
  await page.getByRole('searchbox', { name: 'Search modules' }).fill('');

  // Keybinds.
  await page.getByRole('button', { name: 'HUD settings', exact: true }).click();
  await page.getByRole('button', { name: 'NONE', exact: true }).click();
  await page.keyboard.press('K');
  assert.equal(await page.getByRole('button', { name: 'K', exact: true }).count(), 1);
  await page.keyboard.press('K');
  assert.equal(await page.getByRole('switch', { name: 'Toggle HUD', exact: true }).getAttribute('aria-checked'), 'false');
  await page.keyboard.press('K');

  // Window: move, resize, size stepper, persistence.
  const frame = page.locator('.window-frame');
  const before = await frame.boundingBox();
  const header = await page.locator('.window-header').boundingBox();
  await page.mouse.move(header.x + header.width / 2, header.y + 15);
  await page.mouse.down();
  await page.mouse.move(header.x + header.width / 2 - 120, header.y - 40, { steps: 6 });
  await page.mouse.up();
  const moved = await frame.boundingBox();
  assert.ok(Math.abs(moved.x - before.x + 120) < 4, 'window moves with header drag');
  const corner = await page.locator('.resize-se').boundingBox();
  await page.mouse.move(corner.x + 9, corner.y + 9);
  await page.mouse.down();
  await page.mouse.move(corner.x + 109, corner.y + 69, { steps: 6 });
  await page.mouse.up();
  const resized = await frame.boundingBox();
  assert.ok(resized.width > moved.width + 80, 'window grows from the corner');
  assert.ok(Math.abs(resized.x - moved.x) < 4, 'left edge stays pinned while resizing');
  await page.getByRole('button', { name: 'Make GUI smaller' }).click();
  assert.equal(await page.locator('.size-stepper span').textContent(), '78%');
  const style = await frame.getAttribute('style');
  await page.reload({ waitUntil: 'networkidle' });
  assert.equal(await page.locator('.window-frame').getAttribute('style'), style);
  await page.getByRole('button', { name: 'Reset window size and position' }).click();
  assert.equal(await page.locator('.size-stepper span').textContent(), '82%');

  // Spotify card controls.
  await page.getByRole('switch', { name: 'Show album artwork' }).click();
  assert.equal(await page.locator('.preview-scene .spotify-art').count(), 0);
  await page.getByRole('switch', { name: 'Show album artwork' }).click();
  await page.getByRole('button', { name: 'Next demo track' }).click();
  assert.equal(await page.locator('.preview-scene .spotify-track-name').textContent(), 'Afterglow');

  // Appearance: size preset and accent.
  await page.getByRole('button', { name: 'Appearance', exact: true }).first().click();
  await page.getByRole('button', { name: 'Small', exact: true }).click();
  assert.equal(await page.locator('.size-stepper span').textContent(), '70%');
  await page.getByRole('button', { name: /Electric acid/ }).click();
  assert.equal(await accentOf(), '#c9f36b');
  await page.getByRole('button', { name: 'Reset all' }).click();
  assert.equal(await accentOf(), '#ff6b8a');

  // Live HUD: close the menu, keystrokes react, binds work in game.
  await page.getByRole('button', { name: 'Close client preview' }).click();
  assert.equal(await page.locator('.client-window').count(), 0);
  assert.equal(await page.locator('.game-hud').count(), 1);
  assert.equal(await page.locator('.hud-watermark-card').count(), 1);
  assert.equal(await page.locator('.hud-arraylist span').count(), 2);
  await page.keyboard.down('KeyW');
  assert.equal(await page.locator('.ks-key.down').first().textContent(), 'W');
  await page.keyboard.up('KeyW');
  await page.keyboard.press('3');
  assert.equal(await page.locator('.hud-slots span').nth(2).getAttribute('class'), 'active');

  // HUD editor: drag the Spotify card and persist.
  await page.getByRole('button', { name: /Edit HUD/ }).click();
  assert.equal(await page.getByRole('toolbar', { name: 'HUD layout editor' }).count(), 1);
  const spotify = page.getByRole('button', { name: 'Move Spotify. Drag or use arrow keys.' });
  const sBox = await spotify.boundingBox();
  await page.mouse.move(sBox.x + 60, sBox.y + 30);
  await page.mouse.down();
  await page.mouse.move(sBox.x + 460, sBox.y - 300, { steps: 8 });
  await page.mouse.up();
  const sAfter = await spotify.boundingBox();
  assert.ok(sAfter.x > sBox.x + 300 && sAfter.y < sBox.y - 200, 'Spotify card moves');
  await page.getByRole('button', { name: 'Keystrokes', exact: true }).click();
  assert.equal(await page.locator('.hud-keystrokes').count(), 0);
  await page.getByRole('button', { name: 'Keystrokes', exact: true }).click();
  await page.getByRole('button', { name: 'Done' }).click();
  await page.reload({ waitUntil: 'networkidle' });
  await page.getByRole('button', { name: 'HUD Editor', exact: true }).click();
  const sReloaded = await page.getByRole('button', { name: 'Move Spotify. Drag or use arrow keys.' }).boundingBox();
  assert.ok(Math.abs(sReloaded.x - sAfter.x) < 3, 'HUD layout persists');
  await page.keyboard.press('Escape');
  assert.equal(await page.locator('.client-window').count(), 1);

  await page.getByRole('button', { name: 'Build notes' }).click();
  assert.equal(await page.getByRole('dialog').count(), 1);
  assert.equal((await page.request.get(`${baseUrl}/CLIENT_PORT.md`)).status(), 200);
  await page.keyboard.press('Escape');
  await page.keyboard.press('ShiftRight');
  assert.equal(await page.locator('.client-window').count(), 0);
  await page.keyboard.press('ShiftRight');
  assert.equal(await page.locator('.client-window').count(), 1);
  assert.deepEqual(errors, []);

  const mobile = await browser.newPage({ viewport: { width: 390, height: 844 } });
  await mobile.goto(baseUrl, { waitUntil: 'networkidle' });
  const size = await mobile.locator('body').evaluate((element) => ({ scrollWidth: element.scrollWidth, clientWidth: element.clientWidth }));
  assert.equal(size.scrollWidth, size.clientWidth);
  await mobile.getByRole('button', { name: 'HUD Editor', exact: true }).click();
  assert.equal(await mobile.getByRole('toolbar', { name: 'HUD layout editor' }).count(), 1);
  console.log('Preview smoke checks passed');
} finally {
  await browser.close();
}
