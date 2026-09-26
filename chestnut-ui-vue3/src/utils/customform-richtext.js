import createDOMPurify from 'dompurify';

// Keep this policy aligned with HtmlUtils.cleanRichText(): jsoup Safelist.relaxed()
// plus figure/figcaption/hr and iurl links. Use a private instance for these hooks.
const tags = [
  'a', 'b', 'blockquote', 'br', 'caption', 'cite', 'code', 'col', 'colgroup',
  'dd', 'div', 'dl', 'dt', 'em', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'i',
  'img', 'li', 'ol', 'p', 'pre', 'q', 'small', 'span', 'strike', 'strong',
  'sub', 'sup', 'table', 'tbody', 'td', 'tfoot', 'th', 'thead', 'tr', 'u', 'ul',
  'figure', 'figcaption', 'hr',
];
const attributes = {
  a: ['href', 'title'],
  blockquote: ['cite'],
  col: ['span', 'width'],
  colgroup: ['span', 'width'],
  img: ['align', 'alt', 'height', 'src', 'title', 'width'],
  ol: ['start', 'type'],
  q: ['cite'],
  table: ['summary', 'width'],
  td: ['abbr', 'axis', 'colspan', 'rowspan', 'width'],
  th: ['abbr', 'axis', 'colspan', 'rowspan', 'scope', 'width'],
  ul: ['type'],
};
const protocols = {
  a: ['http:', 'https:', 'ftp:', 'mailto:', 'iurl:'],
  img: ['http:', 'https:', 'iurl:'],
  blockquote: ['http:', 'https:'],
  q: ['http:', 'https:'],
};
const purifier = typeof window === 'undefined' ? null : createDOMPurify(window);

purifier?.addHook('uponSanitizeAttribute', (node, data) => {
  const tag = node.nodeName.toLowerCase();
  if (!Object.hasOwn(attributes, tag) || !attributes[tag].includes(data.attrName)) {
    data.keepAttr = false;
    return;
  }
  if (['href', 'src', 'cite'].includes(data.attrName)) {
    try {
      // DOMPurify has already decoded entities. URL parsing also handles tabs and
      // newlines in schemes; relative URLs use the same safe base as the server.
      const url = new URL(data.attrValue, 'https://sanitizer.invalid/');
      data.keepAttr = protocols[tag].includes(url.protocol);
    } catch {
      data.keepAttr = false;
    }
  }
});

const config = {
  ALLOWED_TAGS: tags,
  ALLOWED_ATTR: [...new Set(Object.values(attributes).flat())],
  ALLOW_ARIA_ATTR: false,
  ALLOW_DATA_ATTR: false,
  ALLOWED_URI_REGEXP: /^(?:(?:https?|ftp|mailto|iurl):|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i,
};

export function sanitizeCustomFormRichText(value) {
  if (!purifier?.isSupported) return '';
  try {
    return purifier.sanitize(value == null ? '' : String(value), config);
  } catch {
    // Never feed the original HTML to v-html or UEditor when cleaning fails.
    return '';
  }
}

export function sanitizeCustomFormData(data, fields) {
  const result = { ...data };
  for (const field of fields) {
    if (field.controlType === 'UEditor' && Object.hasOwn(result, field.code)) {
      result[field.code] = sanitizeCustomFormRichText(result[field.code]);
    }
  }
  return result;
}
