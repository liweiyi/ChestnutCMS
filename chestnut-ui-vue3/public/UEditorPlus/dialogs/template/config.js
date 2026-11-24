/*! UEditorPlus v4.5.0*/
var templates = (function () {
  function escapeText(value) {
    return value.replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function render(html) {
    return html.replace(/\{\{([a-zA-Z0-9]+)\}\}/g, function (_, key) {
      if (typeof lang[key] !== 'string') {
        throw new Error('Missing template translation: ' + key);
      }
      return escapeText(lang[key]);
    });
  }

  return [
    {
      pre: "pre0.png",
      title: lang.blank,
      preHtml: render([
        "<p class=\"ue_t\">&nbsp;{{welcome}}</p>"
      ].join('')),
      html: render([
        "<p class=\"ue_t\">{{welcome}}</p>"
      ].join(''))
    },
    {
      pre: "pre1.png",
      title: lang.blog,
      preHtml: render([
        "<h1 label=\"Title center\" name=\"tc\" style=\"border-bottom-color:#cccccc;border-bottom-width:2px;border-bottom-style:solid;padding:0px 4px 0px 0px;text-align:center;margin:0px 0px 20px;\"><span style=\"color:#c0504d;\">{{blogRangeTitle}}</span></h1>",
        "<p style=\"text-align:center;\"><strong class=\" \">{{blogRangeSubtitle}}</strong></p>",
        "<h3><span class=\" \" style=\"font-family:幼圆\">{{blogRangeDefinition}}</span></h3>",
        "<p style=\"text-indent:2em;\">{{blogInsertPreview}} </p><br />",
        "<h3><span class=\" \" style=\"font-family:幼圆\">{{blogRangeCapabilities}}</span></h3>",
        "<p style=\"text-indent:2em;\">{{blogStylePreview}}</p>"
      ].join('')),
      html: render([
        "<h1 class=\"ue_t\" label=\"Title center\" name=\"tc\" style=\"border-bottom-color:#cccccc;border-bottom-width:2px;border-bottom-style:solid;padding:0px 4px 0px 0px;text-align:center;margin:0px 0px 20px;\"><span style=\"color:#c0504d;\">{{documentTitlePlaceholder}}</span></h1>",
        "<p style=\"text-align:center;\"><strong class=\"ue_t\">{{documentSubtitlePlaceholder}}</strong></p>",
        "<h3><span class=\"ue_t\" style=\"font-family:幼圆\">{{heading1Placeholder}}</span></h3>",
        "<p class=\"ue_t\"  style=\"text-indent:2em;\">{{blogInsertBody}}</p>",
        "<h3><span class=\"ue_t\" style=\"font-family:幼圆\">{{heading2Placeholder}}</span></h3>",
        "<p class=\"ue_t\"  style=\"text-indent:2em;\">{{blogStyleBody}} </p>",
        "<h3><span class=\"ue_t\" style=\"font-family:幼圆\">{{heading3Placeholder}}</span></h3>",
        "<p class=\"ue_t\">{{blogInsertBody}}</p>",
        "<p class=\"ue_t\"><br /></p>"
      ].join(''))
    },
    {
      pre: "pre2.png",
      title: lang.resume,
      preHtml: render([
        "<h1 label=\"Title left\" name=\"tl\" style=\"border-bottom-color:#cccccc;border-bottom-width:2px;border-bottom-style:solid;padding:0px 4px 0px 0px;margin:0px 0px 10px;\"><span style=\"color:#e36c09;\" class=\" \">{{resumePreviewTitle}}</span></h1>",
        "<table width=\"100%\" border=\"1\" bordercolor=\"#95B3D7\" style=\"border-collapse:collapse;\"><tbody><tr><td width=\"100\" style=\"text-align:center;\">",
        "<p><span style=\"background-color:transparent;\">{{photoLine1}}</span><br /></p>",
        "<p>{{photoLine2}}</p>",
        "<p>{{photoLine3}}</p>",
        "<p>{{photoLine4}}</p></td><td>",
        "<p><span style=\"background-color:transparent;\"> {{phoneLabel}}</span><span class=\"ue_t\" style=\"background-color:transparent;\">{{phonePlaceholder}}</span><br /></p>",
        "<p><span style=\"background-color:transparent;\"> {{emailLabel}}</span><span class=\"ue_t\" style=\"background-color:transparent;\">{{emailPlaceholder}}</span><br /></p>",
        "<p><span style=\"background-color:transparent;\"> {{addressLabel}}</span><span class=\"ue_t\" style=\"background-color:transparent;\">{{addressPlaceholder}}</span><br /></p></td></tr></tbody></table>",
        "<h3><span style=\"color:#E36C09;font-size:20px;\">{{desiredPosition}}</span></h3>",
        "<p style=\"text-indent:2em;\" class=\" \">{{webDeveloper}}</p>",
        "<h3><span style=\"color:#e36c09;font-size:20px;\">{{education}}</span></h3>",
        "<p><span style=\"display:none;line-height:0px;\" id=\"_baidu_bookmark_start_26\">﻿</span></p>",
        "<ol style=\"list-style-type:decimal;\">",
        "<li>",
        "<p><span class=\"ue_t\">{{dateRangePlaceholder}}</span> <span class=\"ue_t\">{{schoolPlaceholder}} </span> <span class=\"ue_t\">{{majorPlaceholder}}</span> <span class=\"ue_t\">{{degreePlaceholder}}</span></p></li></ol>",
        "<h3><span style=\"color:#e36c09;font-size:20px;\" class=\"ue_t\">{{workExperience}}</span></h3>",
        "<p><br /></p>"
      ].join('')),
      html: render([
        "<h1 label=\"Title left\" name=\"tl\" style=\"border-bottom-color:#cccccc;border-bottom-width:2px;border-bottom-style:solid;padding:0px 4px 0px 0px;margin:0px 0px 10px;\"><span style=\"color:#e36c09;\" class=\"ue_t\">{{resumeTitlePlaceholder}}</span></h1>",
        "<p><span style=\"color:#e36c09;\"><br /></span></p>",
        "<table width=\"100%\" border=\"1\" bordercolor=\"#95B3D7\" style=\"border-collapse:collapse;\"><tbody><tr><td width=\"200\" style=\"text-align:center;\" class=\"ue_t\">{{photoPlaceholder}}</td><td>",
        "<p><br /></p>",
        "<p> {{phoneLabel}}<span class=\"ue_t\">{{phonePlaceholder}}</span></p>",
        "<p><br /></p>",
        "<p> {{emailLabel}}<span class=\"ue_t\">{{emailPlaceholder}}</span></p>",
        "<p><br /></p>",
        "<p> {{addressLabel}}<span class=\"ue_t\">{{addressPlaceholder}}</span></p>",
        "<p><br /></p></td></tr></tbody></table>",
        "<h3><span style=\"color:#e36c09;font-size:20px;\">{{desiredPosition}}</span></h3>",
        "<p style=\"text-indent:2em;\" class=\"ue_t\">{{desiredPositionPlaceholder}}</p>",
        "<h3><span style=\"color:#e36c09;font-size:20px;\">{{education}}</span></h3>",
        "<p><span style=\"display:none;line-height:0px;\" id=\"_baidu_bookmark_start_26\">﻿</span></p>",
        "<ol style=\"list-style-type:decimal;\">",
        "<li>",
        "<p><span class=\"ue_t\">{{enterDateRangePlaceholder}}</span> <span class=\"ue_t\">{{enterSchoolPlaceholder}} </span> <span class=\"ue_t\">{{enterMajorPlaceholder}}</span> <span class=\"ue_t\">{{enterDegreePlaceholder}}</span></p></li>",
        "<li>",
        "<p><span class=\"ue_t\">{{enterDateRangePlaceholder}}</span> <span class=\"ue_t\">{{enterSchoolPlaceholder}}</span> <span class=\"ue_t\">{{enterMajorPlaceholder}}</span> <span class=\"ue_t\">{{enterDegreePlaceholder}}</span></p></li></ol>",
        "<h3><span style=\"color:#e36c09;font-size:20px;\" class=\"ue_t\">{{workExperience}}</span></h3>",
        "<ol style=\"list-style-type:decimal;\">",
        "<li>",
        "<p><span class=\"ue_t\">{{enterDateRangePlaceholder}}</span> <span class=\"ue_t\">{{companyPlaceholder}}</span> <span class=\"ue_t\">{{positionPlaceholder}}</span> </p></li>",
        "<ol style=\"list-style-type:lower-alpha;\">",
        "<li>",
        "<p><span class=\"ue_t\">{{projectPlaceholder}}</span> <span class=\"ue_t\">{{projectSummaryPlaceholder}}</span></p></li>",
        "<li>",
        "<p><span class=\"ue_t\">{{projectPlaceholder}}</span> <span class=\"ue_t\">{{projectSummaryPlaceholder}}</span></p></li></ol>",
        "<li>",
        "<p><span class=\"ue_t\">{{enterDateRangePlaceholder}}</span> <span class=\"ue_t\">{{companyPlaceholder}}</span> <span class=\"ue_t\">{{positionPlaceholder}}</span> </p></li>",
        "<ol style=\"list-style-type:lower-alpha;\">",
        "<li>",
        "<p><span class=\"ue_t\">{{projectPlaceholder}}</span> <span class=\"ue_t\">{{projectSummaryPlaceholder}}</span></p></li></ol></ol>",
        "<p><span style=\"color:#e36c09;font-size:20px;\">{{skills}}</span></p>",
        "<p style=\"text-indent:2em;\"> &nbsp;<span class=\"ue_t\">{{skillsPlaceholder}}</span><br /></p>"
      ].join(''))
    },
    {
      pre: "pre3.png",
      title: lang.richText,
      preHtml: render([
        "<h1 label=\"Title center\" name=\"tc\" style=\"border-bottom-color:#cccccc;border-bottom-width:2px;border-bottom-style:solid;padding:0px 4px 0px 0px;text-align:center;margin:0px 0px 20px;\" class=\"ue_t\">{{articleTitleHerePlaceholder}}</h1>",
        "<p><img src=\"http://img.baidu.com/hi/youa/y_0034.gif\" width=\"150\" height=\"100\" border=\"0\" hspace=\"0\" vspace=\"0\" style=\"width:150px;height:100px;float:left;\" />{{imageTextLayout}}</p>",
        "<p>{{leftWrapPreview}}</p>",
        "<p>{{leftWrapPreviewInstructions}}</p>",
        "<p><br /></p>",
        "<p><img src=\"http://img.baidu.com/hi/youa/y_0040.gif\" width=\"100\" height=\"100\" border=\"0\" hspace=\"0\" vspace=\"0\" style=\"width:100px;height:100px;float:right;\" /></p>",
        "<p>{{rightWrapExample}}</p>",
        "<p><br /></p>",
        "<p>{{inviteTemplates}}</p>"
      ].join('')),
      html: render([
        "<p><br /></p>",
        "<h1 label=\"Title center\" name=\"tc\" style=\"border-bottom-color:#cccccc;border-bottom-width:2px;border-bottom-style:solid;padding:0px 4px 0px 0px;text-align:center;margin:0px 0px 20px;\" class=\"ue_t\">{{articleTitleHerePlaceholder}}</h1>",
        "<p><img src=\"http://img.baidu.com/hi/youa/y_0034.gif\" width=\"300\" height=\"200\" border=\"0\" hspace=\"0\" vspace=\"0\" style=\"width:300px;height:200px;float:left;\" />{{imageTextLayout}}</p>",
        "<p>{{leftWrapInstructionsTitle}}</p>",
        "<p>{{leftWrapInstructions}}</p>",
        "<p><br /></p>",
        "<p>{{rightWrapInstructionsTitle}}</p>",
        "<p>{{rightWrapInstructions}}</p>",
        "<p><br /></p>",
        "<p>{{centerWrapTitle}}</p>",
        "<p>{{centerWrapInstructions}}</p>",
        "<p><br /></p>",
        "<p><br /></p>",
        "<p><img src=\"http://img.baidu.com/hi/youa/y_0040.gif\" width=\"300\" height=\"300\" border=\"0\" hspace=\"0\" vspace=\"0\" style=\"width:300px;height:300px;float:right;\" /></p>",
        "<p>{{rightWrapExample}}</p>",
        "<p><br /></p>",
        "<p>{{inviteTemplates}}</p>",
        "<p><br /></p>",
        "<p>{{spacer}}</p>",
        "<p><br /></p>",
        "<p>{{spacer}}</p>",
        "<p><br /></p>",
        "<p>{{spacer}}</p>",
        "<p><br /></p>",
        "<p>{{spacer}}</p>",
        "<p><br /></p>",
        "<p>{{spacer}}</p>",
        "<p><br /></p>",
        "<p><br /></p>"
      ].join(''))
    },
    {
      pre: "pre4.png",
      title: lang.sciPapers,
      preHtml: render([
        "<h2 style=\"border-bottom-color:#cccccc;border-bottom-width:2px;border-bottom-style:solid;padding:0px 4px 0px 0px;margin:0px 0px 10px;text-align:center;\" class=\"ue_t\">{{articleTitlePlaceholder}}</h2>",
        "<p><strong><span style=\"font-size:12px;\">{{abstractLabel}}</span></strong><span style=\"font-size:12px;\" class=\"ue_t\">{{abstractPlaceholder}}</span></p>",
        "<p style=\"line-height:1.5em;\"><strong>{{heading1}}</strong></p>",
        "<p style=\"text-indent:2em;\"><span style=\"font-size:14px;\" class=\"ue_t\">{{paperIntro}}</span></p>",
        "<p style=\"line-height:1.5em;\"><strong>{{heading2}}</strong></p>",
        "<ol style=\"list-style-type:lower-alpha;\">",
        "<li>",
        "<p class=\"ue_t\">{{listItem1}}</p></li>",
        "<li>",
        "<p class=\"ue_t\">{{listItem2}}</p></li>",
        "<ol style=\"list-style-type:lower-roman;\">",
        "<li>",
        "<p class=\"ue_t\">{{nestedListItem1}}</p></li>",
        "<li>",
        "<p class=\"ue_t\">{{nestedListItem2}}</p></li></ol>",
        "<li>",
        "<p class=\"ue_t\">{{listItem3}}<br /></p></li></ol>",
        "<p style=\"line-height:1.5em;\"><strong>{{heading3}}</strong></p>",
        "<p style=\"text-indent:2em;\"><span style=\"font-size:14px;\" class=\"ue_t\">{{paperMixedLayout}}</span></p>",
        "<p style=\"text-indent:2em;\"><br /></p>"
      ].join('')),
      html: render([
        "<h2 style=\"border-bottom-color:#cccccc;border-bottom-width:2px;border-bottom-style:solid;padding:0px 4px 0px 0px;margin:0px 0px 10px;text-align:center;\" class=\"ue_t\">{{articleTitlePlaceholder}}</h2>",
        "<p><strong><span style=\"font-size:12px;\">{{abstractLabel}}</span></strong><span style=\"font-size:12px;\" class=\"ue_t\">{{abstractPlaceholder}}</span></p>",
        "<p style=\"line-height:1.5em;\"><strong>{{heading1}}</strong></p>",
        "<p style=\"text-indent:2em;\"><span style=\"font-size:14px;\" class=\"ue_t\">{{paperIntro}}</span></p>",
        "<p style=\"line-height:1.5em;\"><strong>{{heading2}}</strong></p>",
        "<p style=\"text-indent:2em;\"><span style=\"font-size:14px;\" class=\"ue_t\">{{paperListIntro}}</span></p>",
        "<ol style=\"list-style-type:lower-alpha;\">",
        "<li>",
        "<p class=\"ue_t\">{{listItem1}}</p></li>",
        "<li>",
        "<p class=\"ue_t\">{{listItem2}}</p></li>",
        "<ol style=\"list-style-type:lower-roman;\">",
        "<li>",
        "<p class=\"ue_t\">{{nestedListItem1}}</p></li>",
        "<li>",
        "<p class=\"ue_t\">{{nestedListItem2}}</p></li></ol>",
        "<li>",
        "<p class=\"ue_t\">{{listItem3}}<br /></p></li></ol>",
        "<p style=\"line-height:1.5em;\"><strong>{{heading3}}</strong></p>",
        "<p style=\"text-indent:2em;\"><span style=\"font-size:14px;\" class=\"ue_t\">{{paperMixedLayout}}</span></p>",
        "<p style=\"text-indent:2em;\"><span style=\"font-size:14px;\" class=\"ue_t\">{{paperMultiline}}</span></p>",
        "<p style=\"text-indent:2em;\"><span style=\"font-size:14px;\" class=\"ue_t\">{{paperRightImage}}</span></p>",
        "<p style=\"text-indent:2em;\"><span style=\"font-size:14px;\" class=\"ue_t\">{{paperTryText}}</span></p>",
        "<p><br /></p>"
      ].join(''))
    }
  ];
})();
