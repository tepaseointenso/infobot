const SEPARATORS = ["\n\n", "\n", ". ", " "];

export function normalizeText(text: string): string {
  return text
    .replace(/\u00ad|\r/g, "")
    .replace(/-\n(\p{L})/gu, "$1")
    .replace(/[ \t]+/g, " ")
    .replace(/ *\n */g, "\n")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

function split(text: string, size: number, seps: string[]): string[] {
  if (text.length <= size) return [text];
  if (!seps.length) {
    const out: string[] = [];
    for (let i = 0; i < text.length; i += size) out.push(text.slice(i, i + size));
    return out;
  }
  const [sep, ...rest] = seps;
  const pieces = text.split(sep);
  if (pieces.length === 1) return split(text, size, rest);

  const out: string[] = [];
  let current = "";
  for (const piece of pieces) {
    const candidate = current ? `${current}${sep}${piece}` : piece;
    if (candidate.length <= size) {
      current = candidate;
      continue;
    }
    if (current) out.push(current);
    if (piece.length > size) {
      out.push(...split(piece, size, rest));
      current = "";
    } else {
      current = piece;
    }
  }
  if (current) out.push(current);
  return out;
}

/** Split on natural boundaries (paragraph > line > sentence > word) and add a tail overlap. */
export function chunkText(text: string, size = 1000, overlap = 150): string[] {
  const normalized = normalizeText(text);
  if (!normalized) return [];
  const base = split(normalized, size, SEPARATORS)
    .map((p) => p.trim())
    .filter(Boolean);
  if (overlap <= 0 || base.length < 2) return base;

  const chunks = [base[0]];
  for (let i = 1; i < base.length; i++) {
    let tail = base[i - 1].slice(-overlap);
    const cut = tail.indexOf(" ");
    if (cut >= 0 && cut < tail.length - 1) tail = tail.slice(cut + 1);
    chunks.push(`${tail} ${base[i]}`.trim());
  }
  return chunks;
}
