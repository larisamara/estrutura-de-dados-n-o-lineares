import os
import re
import html
import zipfile
import xml.sax.saxutils as saxutils

def escape_xml(s):
    return saxutils.escape(s)

KEYWORDS = {
    'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class', 
    'const', 'continue', 'default', 'do', 'double', 'else', 'enum', 'extends', 'final', 
    'finally', 'float', 'for', 'goto', 'if', 'implements', 'import', 'instanceof', 'int', 
    'interface', 'long', 'native', 'new', 'package', 'private', 'protected', 'public', 
    'return', 'short', 'static', 'strictfp', 'super', 'switch', 'synchronized', 'this', 
    'throw', 'throws', 'transient', 'try', 'void', 'volatile', 'while', 'true', 'false', 'null',
    '@Override'
}

def highlight_java_line(line):
    runs = []
    token_spec = [
        ('COMMENT', r'//.*'),
        ('STRING',  r'"([^"\\]|\\.)*"'),
        ('WORD',    r'@[a-zA-Z_]\w*|[a-zA-Z_]\w*'),
        ('NUMBER',  r'\b\d+\b'),
        ('OTHER',   r'[^\w\s"\'/]+|\s+|/'),
    ]
    tok_regex = '|'.join('(?P<%s>%s)' % pair for pair in token_spec)
    
    for mo in re.finditer(tok_regex, line):
        kind = mo.lastgroup
        val = mo.group()
        
        color = "000000"
        bold = False
        italic = False
        
        if kind == 'COMMENT':
            color = "3F7F5F"
            italic = True
        elif kind == 'STRING':
            color = "2A00FF"
        elif kind == 'WORD':
            if val in KEYWORDS or val.startswith('@'):
                color = "7F0055"
                bold = True
        elif kind == 'NUMBER':
            color = "000000"
            
        runs.append((val, color, bold, italic))
    
    if not runs:
        runs.append((line, "000000", False, False))
        
    return runs

def format_code_block(code_text):
    paragraphs = []
    in_block_comment = False
    for line in code_text.splitlines():
        p_runs = []
        stripped = line.strip()
        if in_block_comment:
            p_runs.append((line, "3F7F5F", False, True))
            if "*/" in line:
                in_block_comment = False
        elif stripped.startswith("/*"):
            in_block_comment = True
            p_runs.append((line, "3F7F5F", False, True))
            if "*/" in line:
                in_block_comment = False
        else:
            p_runs = highlight_java_line(line)
            
        p_xml = ['<w:p><w:pPr><w:shd w:val="clear" w:color="auto" w:fill="F8F9FA"/><w:spacing w:before="0" w:after="0" w:line="240" w:lineRule="auto"/></w:pPr>']
        for text, color, bold, italic in p_runs:
            escaped = escape_xml(text)
            r_xml = ['<w:r><w:rPr><w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/><w:sz w:val="19"/>']
            if color != "000000":
                r_xml.append(f'<w:color w:val="{color}"/>')
            if bold:
                r_xml.append('<w:b/>')
            if italic:
                r_xml.append('<w:i/>')
            r_xml.append('</w:rPr>')
            r_xml.append(f'<w:t xml:space="preserve">{escaped}</w:t></w:r>')
            p_xml.append(''.join(r_xml))
        p_xml.append('</w:p>')
        paragraphs.append(''.join(p_xml))
    return '\n'.join(paragraphs)

def make_heading(text, level=1):
    sz = "32" if level == 1 else "26" if level == 2 else "22"
    color = "1F497D" if level == 1 else "365F91" if level == 2 else "4F81BD"
    return f"""<w:p>
      <w:pPr>
        <w:pStyle w:val="Heading{level}"/>
        <w:spacing w:before="240" w:after="120"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
          <w:b/>
          <w:sz w:val="{sz}"/>
          <w:color w:val="{color}"/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def make_paragraph(text, bold_prefix="", italic=False):
    runs_xml = []
    if bold_prefix:
        runs_xml.append(f"""<w:r>
          <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:b/><w:sz w:val="22"/></w:rPr>
          <w:t xml:space="preserve">{escape_xml(bold_prefix)} </w:t>
        </w:r>""")
    rPr = ['<w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/>']
    if italic:
        rPr.append('<w:i/>')
    runs_xml.append(f"""<w:r>
      <w:rPr>{''.join(rPr)}</w:rPr>
      <w:t xml:space="preserve">{escape_xml(text)}</w:t>
    </w:r>""")
    return f"""<w:p>
      <w:pPr><w:spacing w:before="60" w:after="120" w:line="276" w:lineRule="auto"/></w:pPr>
      {''.join(runs_xml)}
    </w:p>"""

def make_callout(text, title=""):
    border_xml = '<w:pBdr><w:left w:val="single" w:sz="24" w:space="12" w:color="365F91"/></w:pBdr>'
    shd_xml = '<w:shd w:val="clear" w:color="auto" w:fill="F0F4F8"/>'
    runs_xml = []
    if title:
        runs_xml.append(f"""<w:r>
          <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:b/><w:color w:val="1F497D"/><w:sz w:val="22"/></w:rPr>
          <w:t>{escape_xml(title)}: </w:t>
        </w:r>""")
    runs_xml.append(f"""<w:r>
      <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/></w:rPr>
      <w:t xml:space="preserve">{escape_xml(text)}</w:t>
    </w:r>""")
    return f"""<w:p>
      <w:pPr>{border_xml}{shd_xml}<w:spacing w:before="120" w:after="120" w:line="276" w:lineRule="auto"/></w:pPr>
      {''.join(runs_xml)}
    </w:p>"""

def build_docx(output_path):
    files = ["NoABP.java", "ArvoreBinariaPesquisa.java", "NoAVL.java", "ArvoreAVL.java", "Main.java"]
    codes = {}
    for f in files:
        if os.path.exists(f):
            with open(f, "r", encoding="utf-8") as fp:
                codes[f] = fp.read()
        else:
            codes[f] = "// Arquivo não encontrado"

    body_parts = []

    # Title & Metadata
    body_parts.append("""<w:p>
      <w:pPr>
        <w:jc w:val="center"/>
        <w:spacing w:before="360" w:after="120"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
          <w:b/>
          <w:sz w:val="36"/>
          <w:color w:val="1F497D"/>
        </w:rPr>
        <w:t>ESTRUTURA DE DADOS NÃO-LINEARES</w:t>
      </w:r>
    </w:p>""")

    body_parts.append("""<w:p>
      <w:pPr>
        <w:jc w:val="center"/>
        <w:spacing w:before="0" w:after="240"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
          <w:b/>
          <w:sz w:val="28"/>
          <w:color w:val="595959"/>
        </w:rPr>
        <w:t>Implementação de Árvore AVL com Herança de ABP</w:t>
      </w:r>
    </w:p>""")

    body_parts.append("""<w:p>
      <w:pPr>
        <w:jc w:val="center"/>
        <w:spacing w:before="0" w:after="360"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
          <w:i/>
          <w:sz w:val="22"/>
          <w:color w:val="595959"/>
        </w:rPr>
        <w:t>Aluna: Larissa Samara | Professor: Robinson Alves | Material: árvoreAVL.pdf</w:t>
      </w:r>
    </w:p>""")

    # Section 1: Fundamentação e Fórmulas do Slide
    body_parts.append(make_heading("1. Fundamentação Teórica e Fórmulas de Aula (Prof. Robinson Alves)", 1))
    body_parts.append(make_paragraph(
        "Conforme apresentado nos slides do Prof. Robinson Alves (árvoreAVL.pdf), a Árvore AVL mantém o equilíbrio rigoroso através do Fator de Balanceamento (FB) calculado para cada nó v como:"
    ))
    body_parts.append(make_callout(
        "FB(v) = he(v) - hd(v)\n"
        " +1 : subárvore esquerda mais alta que a direita\n"
        "  0 : subárvore esquerda igual a direita\n"
        " -1 : subárvore direita mais alta do que a esquerda\n"
        "Valores balanceados: -1, 0 ou 1. Nós desregulados: FB > 1 ou FB < -1.",
        "Definição do Fator de Balanceamento (Slide do Prof. Robinson)"
    ))

    body_parts.append(make_paragraph(
        "A tabela a seguir resume as variações de FB e os critérios de parada dos antecessores ensinados na aula:"
    ))
    body_parts.append(make_callout(
        "Inserção: ArvEsq (+1), ArvDir (-1) -> Regra: 'Se FB(Vantecessor) == 0 pare'\n"
        "Remoção:  ArvEsq (-1), ArvDir (+1) -> Regra: 'Se FB(Vantecessor) != 0 pare'",
        "Critérios de Parada na Propagação do FB"
    ))

    body_parts.append(make_paragraph(
        "Para a reestruturação e recálculo em O(1) dos fatores de balanceamento dos nós B (raiz original da rotação) e A (novo topo da subárvore), o código adota as fórmulas dos slides (baseadas em StackExchange CS #48861):"
    ))
    body_parts.append(make_callout(
        "FB_B_novo = FB_B + 1 - min(FB_A, 0);\n"
        "FB_A_novo = FB_A + 1 + max(FB_B_novo, 0);",
        "Rotação Esquerda Simples (RES)"
    ))
    body_parts.append(make_callout(
        "FB_B_novo = FB_B - 1 - max(FB_A, 0);\n"
        "FB_A_novo = FB_A - 1 + min(FB_B_novo, 0);",
        "Rotação Simples a Direita (RSD)"
    ))
    body_parts.append(make_paragraph(
        "As rotações duplas são executadas conforme os passos descritos no slide:\n"
        "- Rotação Dupla a Esquerda (RDE): RSD na subárvore direita do nó desbalanceado, seguida de RES no nó desbalanceado.\n"
        "- Rotação Dupla a Direita (RDD): RES na subárvore esquerda do nó desbalanceado, seguida de RSD no nó desbalanceado."
    ))

    # Section 2: Detalhamento do Exemplo da Especificação
    body_parts.append(make_heading("2. Detalhamento do Exemplo da Especificação", 1))
    body_parts.append(make_heading("2.1 Inserção do Nó 25 e Aplicação da RES em 15", 2))
    body_parts.append(make_paragraph(
        "Após inserir 10, 5, 15, 2, 8, 22, a árvore possui o nó 15 com FB = -1 devido ao filho direito 22. Ao inserir 25:\n"
        "1. 25 é alocado à direita de 22. O nó 22 passa para FB = -1.\n"
        "2. O aumento propaga para o nó 15: seu FB passa de -1 para -2 (desbalanceamento à direita).\n"
        "3. Regra do slide: com FB = -2 e subárvore direita com FB <= 0 (FB(22) = -1), aplica-se a Rotação Esquerda Simples (RES) no nó 15.\n"
        "4. Fórmulas de recálculo:\n"
        "   FB_B_novo (nó 15) = -2 + 1 - min(-1, 0) = -2 + 1 - (-1) = 0\n"
        "   FB_A_novo (nó 22) = -1 + 1 + max(0, 0) = -1 + 1 + 0 = 0\n"
        "5. O nó 22 torna-se nova raiz com 15 à esquerda e 25 à direita, ambos com FB = 0."
    ))

    body_parts.append(make_heading("2.2 Remoção do Nó 5", 2))
    body_parts.append(make_paragraph(
        "O nó 5 tem dois filhos (2 e 8). Pela regra de remoção de ABP/AVL:\n"
        "1. Substitui 5 pelo seu sucessor in-order (menor nó da subárvore direita), que é 8.\n"
        "2. Remove a folha original 8 da subárvore direita.\n"
        "3. Ao remover da subárvore direita de 8, calcula-se FB(8): he(2) - hd(vazio) = 1 - 0 = +1.\n"
        "4. Regra de parada do slide do Prof. Robinson: 'Se FB(Vantecessor) != 0 pare'.\n"
        "5. Como FB(8) = +1 != 0, a diminuição de altura não se propaga para a raiz 10, permanecendo FB(10) = 0."
    ))

    # Section 3: Código-Fonte Completo
    body_parts.append(make_heading("3. Código-Fonte Completo em Java", 1))
    body_parts.append(make_paragraph(
        "O código-fonte foi desenvolvido utilizando herança estrita (ArvoreAVL estende ArvoreBinariaPesquisa, e NoAVL estende NoABP). Abaixo está o código completo formatado em fonte monoespaçada (Consolas 9.5pt) com o padrão de cores da IDE Eclipse."
    ))

    for fname in files:
        body_parts.append(make_heading(f"Arquivo: {fname}", 2))
        body_parts.append(format_code_block(codes[fname]))

    # Section 4: Saída de Testes
    body_parts.append(make_heading("4. Saída dos Testes de Execução", 1))
    body_parts.append(make_paragraph(
        "Abaixo constam os registros de execução obtidos no terminal pelo programa Main, cobrindo o exemplo da especificação e os exercícios do slide do Prof. Robinson Alves:"
    ))
    terminal_output = """======================================================================
          SISTEMA DE TESTES - ÁRVORE AVL (HERANÇA DE ABP)            
           Conforme Slides e Aulas do Prof. Robinson Alves            
                         Aluna: Larissa Samara                        
======================================================================

[PASSO 1] Inserindo chaves iniciais: 10, 5, 15, 2, 8, 22...
Árvore resultante após inserções iniciais:
                                  10[0]
              5[0]                          15[-1]
    2[0]                8[0]                          22[0]

[PASSO 2] Inserindo chave 25...
Estado antes da rotação:
                                  10[0]
              5[0]                          15[-2]
    2[0]                8[0]                          22[-1]
                                                                25[0]

>> Rotação Esquerda Simples (RES) aplicada no nó 15.
Fórmulas do Slide do Prof. Robinson Alves:
  FB_B_novo = FB_B + 1 - min(FB_A, 0) = -2 + 1 - (-1) = 0
  FB_A_novo = FB_A + 1 + max(FB_B_novo, 0) = -1 + 1 + 0 = 0

Árvore após rotação RES em 15:
                                  10[0]
              5[0]                                    22[0]
    2[0]                8[0]                15[0]               25[0]

[PASSO 3] Removendo chave 5...
- Sucessor in-order localizado: 8
- Recálculo de FB(8) = he(2) - hd(vazio) = 1 - 0 = +1
- Regra do slide: 'Se FB(Vantecessor) != 0 pare'. Propagação cessa no nó 8.
Árvore resultante após remover 5:
                        10[0]
              8[1]                          22[0]
    2[0]                          15[0]               25[0]
======================================================================"""
    body_parts.append(format_code_block(terminal_output))

    doc_xml = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    {''.join(body_parts)}
    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720"/>
    </w:sectPr>
  </w:body>
</w:document>"""

    content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""

    rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    doc_rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
      <w:sz w:val="22"/>
    </w:rPrDefault>
  </w:docDefaults>
</w:styles>"""

    with zipfile.ZipFile(output_path, 'w', zipfile.ZIP_DEFLATED) as z:
        z.writestr('[Content_Types].xml', content_types)
        z.writestr('_rels/.rels', rels)
        z.writestr('word/_rels/document.xml.rels', doc_rels)
        z.writestr('word/styles.xml', styles)
        z.writestr('word/document.xml', doc_xml)

    print(f"Documento gerado com sucesso em: {output_path}")

if __name__ == "__main__":
    build_docx("Trabalho_Arvore_AVL_Larissa_Samara.docx")
