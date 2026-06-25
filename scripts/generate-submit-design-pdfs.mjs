import { execFileSync, spawnSync } from 'node:child_process';
import { createRequire } from 'node:module';
import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';

const root = process.cwd();
const sourceDir = path.join(root, 'docs/final-report/01-design-doc');
const screenshotDir = path.join(root, 'docs/final-report/04-assets/screenshots');
const tmpDir = path.join(root, 'tmp/pdfs/yumyum-submit-design-docs');
const mdDir = path.join(tmpDir, 'md');
const htmlDir = path.join(tmpDir, 'html');
const mermaidDir = path.join(tmpDir, 'mermaid');
const tmpScreenshotDir = path.join(tmpDir, 'screenshots');
const submitDir = path.join(root, 'submit/design-docs');

const docs = [
  {
    slug: '01_requirements',
    title: '요구사항 정의서',
    source: 'requirements.md',
    summary: '서비스 목표, 사용자 요구사항, 시스템/비기능 요구사항, 데모 성공 기준',
  },
  {
    slug: '02_use_case',
    title: 'Use-Case 다이어그램',
    source: 'use-case.md',
    summary: '사용자, 관리자, 외부 API actor와 핵심 use-case 흐름',
  },
  {
    slug: '03_class_diagram',
    title: '클래스 다이어그램',
    source: 'class-diagram.md',
    summary: '도메인 계층, provider/client 경계, mock/live/fallback 협력 구조',
  },
  {
    slug: '04_erd',
    title: 'ER 다이어그램',
    source: 'erd.md',
    summary: '사용자, 냉장고, 식단, 알림, 관리자 테이블 관계와 데이터 흐름',
    landscape: true,
  },
  {
    slug: '05_wbs_gantt',
    title: 'WBS & 간트 차트',
    source: 'wbs-gantt.md',
    summary: '기획, 구현, 안정화, 문서화 단계별 일정과 리스크 대응',
    landscape: true,
  },
  {
    slug: '06_screen_design',
    title: '화면 설계서',
    source: 'screen-design.md',
    summary: '사용자/관리자 화면 흐름, 캡처 기준, 주요 화면별 설계 의도',
    landscape: true,
  },
  {
    slug: '07_references',
    title: '기타 참고 문서',
    source: 'references.md',
    summary: '실행 환경, 주요 API, 검증 명령, 제출 전 체크리스트',
  },
];

const screenshotCaptions = [
  ['01-login.png', '로그인'],
  ['02-onboarding.png', '온보딩'],
  ['03-dashboard.png', '대시보드'],
  ['04-inventory-list.png', '냉장고 목록'],
  ['05-inventory-add.png', '재고 등록'],
  ['06-inventory-detail.png', '재고 상세'],
  ['07-meal-log.png', '식단 기록'],
  ['08-recipe-recommendation.png', '레시피 추천'],
  ['09-notifications.png', '알림'],
  ['10-admin-login.png', '관리자 로그인'],
  ['11-admin-dashboard.png', '관리자 대시보드'],
  ['12-admin-operations.png', '관리자 운영 화면'],
];

const require = createRequire(import.meta.url);
const puppeteer = loadPuppeteer();

function loadPuppeteer() {
  try {
    return require('puppeteer');
  } catch {
    const globalRoot = execFileSync('npm', ['root', '-g'], { encoding: 'utf8' }).trim();
    return require(path.join(globalRoot, '@mermaid-js/mermaid-cli/node_modules/puppeteer'));
  }
}

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function shellQuote(value) {
  return value.replaceAll('\\', '\\\\').replaceAll('"', '\\"');
}

function renderMermaid(inputPath, outputPath) {
  const result = spawnSync('mmdc', [
    '-i',
    inputPath,
    '-o',
    outputPath,
    '-b',
    'white',
    '-s',
    '2',
  ], {
    cwd: root,
    encoding: 'utf8',
  });

  if (result.status !== 0) {
    throw new Error(`Mermaid render failed for ${inputPath}\n${result.stdout}\n${result.stderr}`);
  }
}

function preprocessMarkdown(doc) {
  const sourcePath = path.join(sourceDir, doc.source);
  const original = fs.readFileSync(sourcePath, 'utf8');
  let diagramIndex = 0;

  const withRenderedDiagrams = original.replace(/```mermaid\n([\s\S]*?)\n```/g, (_match, mermaidSource) => {
    diagramIndex += 1;
    const index = String(diagramIndex).padStart(2, '0');
    const mmdPath = path.join(mermaidDir, `${doc.slug}_${index}.mmd`);
    const pngPath = path.join(mermaidDir, `${doc.slug}_${index}.png`);
    fs.writeFileSync(mmdPath, `${mermaidSource.trim()}\n`);
    renderMermaid(mmdPath, pngPath);
    const relativePngPath = path.relative(htmlDir, pngPath);
    const caption = `${doc.title} 다이어그램 ${diagramIndex}`;
    return [
      `<figure class="diagram-figure">`,
      `  <img src="${shellQuote(relativePngPath)}" alt="${shellQuote(caption)}" />`,
      `  <figcaption>${caption}</figcaption>`,
      `</figure>`,
    ].join('\n');
  });

  const injected = doc.slug === '06_screen_design'
    ? `${withRenderedDiagrams}\n\n${screenGalleryMarkdown()}`
    : withRenderedDiagrams;

  const preprocessedPath = path.join(mdDir, `${doc.slug}.md`);
  fs.writeFileSync(preprocessedPath, injected);
  return preprocessedPath;
}

function screenGalleryMarkdown() {
  const figures = screenshotCaptions
    .map(([filename, caption]) => {
      const sourcePath = path.join(screenshotDir, filename);
      const targetPath = path.join(tmpScreenshotDir, filename);
      fs.copyFileSync(sourcePath, targetPath);
      const relativePath = path.relative(htmlDir, targetPath);
      return [
        `<figure class="screenshot-card">`,
        `  <img src="${shellQuote(relativePath)}" alt="${shellQuote(caption)} 화면 캡처" />`,
        `  <figcaption>${caption}</figcaption>`,
        `</figure>`,
      ].join('\n');
    })
    .join('\n');

  return [
    '## 화면 캡처 자료',
    '',
    '<section class="screenshot-grid">',
    figures,
    '</section>',
  ].join('\n');
}

function pandocToHtmlFragment(markdownPath) {
  return execFileSync('pandoc', [
    '--from',
    'gfm+raw_html',
    '--to',
    'html',
    '--wrap=none',
    markdownPath,
  ], {
    cwd: root,
    encoding: 'utf8',
    maxBuffer: 32 * 1024 * 1024,
  });
}

function htmlDocument(doc, body) {
  const generatedAt = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    timeZone: 'Asia/Seoul',
  }).format(new Date());

  return `<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>YumYum - ${escapeHtml(doc.title)}</title>
  <style>
    ${css(doc)}
  </style>
</head>
<body>
  <main class="page-shell">
    <section class="cover">
      <p class="eyebrow">YumYum 설계 문서</p>
      <h1>${escapeHtml(doc.title)}</h1>
      <p class="summary">${escapeHtml(doc.summary)}</p>
      <dl class="meta-grid">
        <div><dt>프로젝트</dt><dd>YumYum</dd></div>
        <div><dt>기준 커밋</dt><dd>a963b9d</dd></div>
        <div><dt>생성일</dt><dd>${generatedAt}</dd></div>
      </dl>
    </section>
    <article class="content">
      ${body}
    </article>
  </main>
</body>
</html>`;
}

function css(doc) {
  const regularFont = '/Users/myknow/Library/Fonts/Pretendard-Regular.ttf';
  const semiBoldFont = '/Users/myknow/Library/Fonts/Pretendard-SemiBold.ttf';
  const boldFont = '/Users/myknow/Library/Fonts/Pretendard-Bold.ttf';
  return `
    @font-face {
      font-family: "Pretendard";
      font-weight: 400;
      src: url("file://${regularFont}") format("truetype");
    }
    @font-face {
      font-family: "Pretendard";
      font-weight: 600;
      src: url("file://${semiBoldFont}") format("truetype");
    }
    @font-face {
      font-family: "Pretendard";
      font-weight: 700;
      src: url("file://${boldFont}") format("truetype");
    }
    @page {
      size: A4 ${doc.landscape ? 'landscape' : 'portrait'};
    }
    * {
      box-sizing: border-box;
    }
    html {
      color: #15201c;
      font-family: "Pretendard", "Apple SD Gothic Neo", "Noto Sans CJK KR", sans-serif;
      font-size: 13px;
      line-height: 1.65;
      background: #ffffff;
    }
    body {
      margin: 0;
      background: #ffffff;
    }
    .page-shell {
      width: 100%;
    }
    .cover {
      border: 1px solid #d8e5df;
      border-radius: 12px;
      padding: 28px 30px;
      margin: 0 0 24px;
      background: linear-gradient(135deg, #f7fbf8 0%, #ffffff 64%, #eef7f2 100%);
      break-inside: avoid;
    }
    .eyebrow {
      margin: 0 0 8px;
      color: #178456;
      font-size: 12px;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }
    h1 {
      margin: 0;
      color: #0f1f1a;
      font-size: 30px;
      line-height: 1.2;
      letter-spacing: 0;
    }
    .summary {
      max-width: 720px;
      margin: 12px 0 22px;
      color: #52615c;
      font-size: 14px;
    }
    .meta-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 10px;
      margin: 0;
    }
    .meta-grid div {
      border: 1px solid #dfe9e4;
      border-radius: 8px;
      padding: 10px 12px;
      background: rgba(255, 255, 255, 0.8);
    }
    .meta-grid dt {
      color: #6b7a75;
      font-size: 10px;
      font-weight: 700;
      margin: 0 0 3px;
    }
    .meta-grid dd {
      margin: 0;
      color: #17231f;
      font-weight: 700;
    }
    .content h1 {
      display: none;
    }
    h2 {
      margin: 30px 0 12px;
      padding-top: 6px;
      color: #15201c;
      font-size: 20px;
      line-height: 1.35;
      letter-spacing: 0;
      border-top: 2px solid #18231f;
      break-after: avoid;
    }
    h3 {
      margin: 22px 0 8px;
      color: #1d342c;
      font-size: 15px;
      break-after: avoid;
    }
    p {
      margin: 8px 0;
      color: #26332f;
    }
    ul, ol {
      margin: 8px 0 12px 20px;
      padding: 0;
    }
    li {
      margin: 3px 0;
    }
    strong {
      font-weight: 700;
      color: #101815;
    }
    code {
      border: 1px solid #dce5e1;
      border-radius: 5px;
      padding: 1px 5px;
      background: #f5f8f6;
      color: #15392b;
      font-family: "SFMono-Regular", Consolas, monospace;
      font-size: 0.9em;
    }
    pre {
      margin: 12px 0;
      padding: 14px 16px;
      overflow: hidden;
      border: 1px solid #dce5e1;
      border-radius: 8px;
      background: #f7faf8;
      white-space: pre-wrap;
      break-inside: avoid;
    }
    pre code {
      border: 0;
      padding: 0;
      background: transparent;
      color: #1a2a25;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 12px 0 18px;
      table-layout: fixed;
      break-inside: avoid;
      font-size: 11.5px;
    }
    th, td {
      border: 1px solid #d9e3df;
      padding: 8px 9px;
      vertical-align: top;
      word-break: keep-all;
      overflow-wrap: anywhere;
    }
    th {
      background: #eff7f2;
      color: #18352a;
      font-weight: 700;
    }
    tr:nth-child(even) td {
      background: #fbfdfc;
    }
    .diagram-figure {
      margin: ${doc.landscape ? '10px 0 16px' : '18px 0 24px'};
      padding: ${doc.landscape ? '8px' : '12px'};
      border: 1px solid #d8e5df;
      border-radius: 10px;
      background: #ffffff;
      break-inside: ${doc.landscape ? 'auto' : 'avoid'};
      text-align: center;
    }
    .diagram-figure img {
      display: block;
      max-width: 100%;
      max-height: ${doc.landscape ? '470px' : '650px'};
      margin: 0 auto;
      object-fit: contain;
    }
    figcaption {
      margin-top: 8px;
      color: #60716b;
      font-size: 11px;
      font-weight: 600;
      text-align: center;
    }
    .screenshot-grid {
      display: grid;
      grid-template-columns: repeat(${doc.landscape ? '3' : '2'}, minmax(0, 1fr));
      gap: 12px;
      margin-top: 12px;
    }
    .screenshot-card {
      margin: 0;
      padding: 10px;
      border: 1px solid #d8e5df;
      border-radius: 10px;
      background: #ffffff;
      break-inside: avoid;
    }
    .screenshot-card img {
      display: block;
      width: 100%;
      border: 1px solid #e5ece8;
      border-radius: 7px;
    }
    .screenshot-card figcaption {
      margin-top: 6px;
    }
    blockquote {
      margin: 14px 0;
      padding: 10px 14px;
      border-left: 4px solid #26a269;
      background: #f3faf6;
      color: #30423b;
    }
    hr {
      border: 0;
      border-top: 1px solid #d8e5df;
      margin: 22px 0;
    }
  `;
}

function escapeHtml(value) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;');
}

async function writePdf(browser, doc, htmlPath, pdfPath) {
  const page = await browser.newPage();
  await page.goto(`file://${htmlPath}`, { waitUntil: 'networkidle0' });
  await page.pdf({
    path: pdfPath,
    format: 'A4',
    landscape: Boolean(doc.landscape),
    printBackground: true,
    displayHeaderFooter: true,
    headerTemplate: '<div></div>',
    footerTemplate: `
      <div style="width:100%;font-family:Pretendard, sans-serif;font-size:8px;color:#77847f;padding:0 16mm;display:flex;justify-content:space-between;">
        <span>YumYum 설계 문서 - ${escapeHtml(doc.title)}</span>
        <span><span class="pageNumber"></span> / <span class="totalPages"></span></span>
      </div>
    `,
    margin: {
      top: '16mm',
      right: '15mm',
      bottom: '18mm',
      left: '15mm',
    },
  });
  await page.close();
}

function writeSubmitReadme(pdfPaths) {
  const lines = [
    '# YumYum 제출용 설계문서 PDF',
    '',
    '이 폴더는 제출용으로 꾸민 설계문서 PDF 산출물을 모아둔 위치입니다.',
    '',
    '## 파일 목록',
    '',
    ...pdfPaths.map((pdfPath) => `- \`${path.basename(pdfPath)}\``),
    '',
    '## 생성 기준',
    '',
    '- 원본: `docs/final-report/01-design-doc/`',
    '- Mermaid 다이어그램은 PNG로 렌더링 후 PDF에 삽입',
    '- 화면 설계서는 실제 화면 캡처 갤러리 포함',
    '- 기준 커밋: `a963b9d feat: stabilize demo mvp flows`',
  ];
  fs.writeFileSync(path.join(submitDir, 'README.md'), `${lines.join('\n')}\n`);
}

async function main() {
  [tmpDir, mdDir, htmlDir, mermaidDir, tmpScreenshotDir, submitDir].forEach(ensureDir);

  const browser = await puppeteer.launch({
    headless: 'new',
    args: [
      '--allow-file-access-from-files',
      '--disable-dev-shm-usage',
      '--no-sandbox',
    ],
  });

  const pdfPaths = [];
  try {
    for (const doc of docs) {
      const preprocessedPath = preprocessMarkdown(doc);
      const htmlFragment = pandocToHtmlFragment(preprocessedPath);
      const html = htmlDocument(doc, htmlFragment);
      const htmlPath = path.join(htmlDir, `${doc.slug}.html`);
      const pdfPath = path.join(submitDir, `YumYum_${doc.slug}_${doc.title.replaceAll(' ', '_')}.pdf`);
      fs.writeFileSync(htmlPath, html);
      await writePdf(browser, doc, htmlPath, pdfPath);
      pdfPaths.push(pdfPath);
      console.log(`created ${path.relative(root, pdfPath)}`);
    }
  } finally {
    await browser.close();
  }

  const combinedPath = path.join(submitDir, 'YumYum_00_design_documents_combined.pdf');
  const unite = spawnSync('pdfunite', [...pdfPaths, combinedPath], {
    cwd: root,
    encoding: 'utf8',
  });
  if (unite.status !== 0) {
    throw new Error(`pdfunite failed\n${unite.stdout}\n${unite.stderr}`);
  }

  writeSubmitReadme([combinedPath, ...pdfPaths]);
  console.log(`created ${path.relative(root, combinedPath)}`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
