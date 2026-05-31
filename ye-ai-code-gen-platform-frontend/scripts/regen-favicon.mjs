import sharp from 'sharp';
import toIco from 'to-ico';
import { writeFileSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));

const SVG_LOGO = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
  <defs>
    <linearGradient id="faviconGradient" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#D32F2F"/>
      <stop offset="100%" stop-color="#FF7043"/>
    </linearGradient>
  </defs>
  <path 
    d="M50 15 L60 25 L75 22 L68 38 L82 43 L70 52 L82 62 L68 67 L75 82 L60 78 L50 88 L40 78 L25 82 L32 67 L18 62 L30 52 L18 43 L32 38 L25 22 L40 25 Z" 
    fill="url(#faviconGradient)"
    stroke="#C62828"
    stroke-width="2"
  />
  <path d="M50 25 L50 75" stroke="#FFEBEE" stroke-width="2" stroke-linecap="round"/>
  <path d="M50 35 L42 28 M50 45 L38 38 M50 55 L38 62 M50 65 L42 72" stroke="#FFEBEE" stroke-width="1.5" stroke-linecap="round"/>
  <path d="M50 35 L58 28 M50 45 L62 38 M50 55 L62 62 M50 65 L58 72" stroke="#FFEBEE" stroke-width="1.5" stroke-linecap="round"/>
  <line x1="15" y1="50" x2="25" y2="50" stroke="#FF7043" stroke-width="2" stroke-linecap="round" opacity="0.9"/>
  <line x1="12" y1="40" x2="20" y2="42" stroke="#FF7043" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
  <line x1="12" y1="60" x2="20" y2="58" stroke="#FF7043" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
  <line x1="75" y1="50" x2="85" y2="50" stroke="#FF7043" stroke-width="2" stroke-linecap="round" opacity="0.9"/>
  <line x1="80" y1="40" x2="88" y2="42" stroke="#FF7043" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
  <line x1="80" y1="60" x2="88" y2="58" stroke="#FF7043" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
</svg>`;

async function main() {
  const sizes = [16, 32, 48, 64];
  const pngBuffers = [];

  for (const size of sizes) {
    const buf = await sharp(Buffer.from(SVG_LOGO))
      .resize(size, size)
      .png()
      .toBuffer();
    pngBuffers.push(buf);
  }

  const icoBuffer = await toIco(pngBuffers, { resize: true });
  const outputPath = join(__dirname, '..', 'public', 'favicon.ico');
  writeFileSync(outputPath, icoBuffer);
  console.log('favicon.ico regenerated successfully at', outputPath);
}

main().catch(console.error);
