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
    # Simple regex tokenizer for Java code lines
    # Tokens: comments, strings, words/identifiers, symbols/whitespace
    token_spec = [
        ('COMMENT', r'//.*'),
        ('STRING',  r'"([^"\\]|\\.)*"'),
        ('WORD',    r'@[a-zA-Z_]\w*|[a-zA-Z_]\w*'),
        ('NUMBER',  r'\b\d+\b'),
        ('OTHER',   r'[^\w\s"\'/]+|\s+|/'),
    ]
    tok_regex = '|'.join('(?P<%s>%s)' % pair for pair in token_spec)
    
    pos = 0
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
    # Read Java files
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
        <w:t>Autora: Larissa Samara | Linguagem: Java | Complexidade: O(log n)</w:t>
      </w:r>
    </w:p>""")

    # Section 1: Apresentação Teórica e Fórmulas
    body_parts.append(make_heading("1. Fundamentação Teórica e Fórmulas de Recálculo", 1))
    body_parts.append(make_paragraph(
        "A Árvore AVL é uma árvore binária de pesquisa autobalanceada em que a diferença entre as alturas das subárvores esquerda e direita de qualquer nó (denominada Fator de Balanceamento - FB) é de no máximo 1 em valor absoluto. Isso garante que a altura máxima da árvore seja h <= 1,44 * log2(n), conferindo complexidade estritamente O(log n) para as operações básicas de busca, inclusão e remoção."
    ))

    body_parts.append(make_callout(
        "FB(u) = altura(subárvore esquerda) - altura(subárvore direita). Portanto, valores positivos indicam que a esquerda é mais alta (+1), zero indica equilíbrio perfeito (0), e valores negativos indicam que a direita é mais alta (-1). Desbalanceamentos ocorrem quando FB atinge +2 ou -2.",
        "Definição do Fator de Balanceamento"
    ))

    body_parts.append(make_paragraph(
        "Conforme solicitado na especificação da disciplina, para garantir que as rotações executem estritamente em tempo constante O(1) sem necessidade de recalcular alturas a partir das folhas, foram empregadas as fórmulas deduzidas em aula:"
    ))

    body_parts.append(make_callout(
        "FB'(A) = FB(A) + 1 - min(FB(B), 0)\nFB'(B) = FB(B) + 1 + max(FB'(A), 0)",
        "Fórmulas para Rotação Simples para a Esquerda (S.E.)"
    ))

    body_parts.append(make_callout(
        "FB'(A) = FB(A) - 1 - max(FB(B), 0)\nFB'(B) = FB(B) - 1 + min(FB'(A), 0)",
        "Fórmulas para Rotação Simples para a Direita (S.D.)"
    ))

    # Section 2: Detalhamento dos Casos do Exemplo
    body_parts.append(make_heading("2. Detalhamento Passo a Passo do Exemplo da Especificação", 1))

    body_parts.append(make_heading("2.1 Inserções Iniciais e Inserção do Nó 25 (Rotação S.E. em 15)", 2))
    body_parts.append(make_paragraph(
        "Inicialmente, inserem-se as chaves 10, 5, 15, 2, 8, 22. A árvore resultante apresenta equilíbrio em todos os nós, com exceção do nó 15 que possui FB = -1 devido ao seu filho direito 22."
    ))
    body_parts.append(make_paragraph(
        "Ao inserir a chave 25, ela é posicionada como filha à direita do nó 22. No retorno da recursão (backtracking):",
        "Propagação do desbalanceamento:"
    ))
    body_parts.append(make_paragraph(
        "- O nó 22 tem sua subárvore direita acrescida de altura: seu FB passa de 0 para -1.\n"
        "- O nó 15, que já possuía FB = -1, recebe a propagação do aumento de altura da subárvore direita: seu FB atinge -2 (desbalanceamento detectado à direita).\n"
        "- Como os sinais de FB(15) = -2 e FB(22) = -1 são concordantes (ambos negativos), aplica-se a Rotação Simples para a Esquerda (S.E.) com pivô no nó 15."
    ))
    body_parts.append(make_callout(
        "FB'(15) = -2 + 1 - min(-1, 0) = -2 + 1 - (-1) = 0\n"
        "FB'(22) = -1 + 1 + max(0, 0) = 0\n"
        "Resultado: O nó 22 torna-se a nova raiz da subárvore com 15 à esquerda e 25 à direita, ambos com FB = 0.",
        "Aplicação das Fórmulas de Recálculo em S.E."
    ))

    body_parts.append(make_heading("2.2 Remoção do Nó 5 e Substituição pelo Sucessor", 2))
    body_parts.append(make_paragraph(
        "O nó 5 possui dois filhos (2 à esquerda e 8 à direita). Pela regra clássica de ABP/AVL com 2 filhos, o nó é substituído pelo seu sucessor in-order (o menor elemento da sua subárvore direita, que neste caso é a folha 8):",
        "Etapas da remoção:"
    ))
    body_parts.append(make_paragraph(
        "1. A chave 8 substitui o valor 5 no nó de destino.\n"
        "2. O nó original 8 (que era folha) é removido da subárvore direita.\n"
        "3. Ao remover da subárvore direita do nó 8 recém-alocado, a altura da direita diminui de 1 para 0.\n"
        "4. Como a subárvore esquerda possui o nó 2 (altura 1), o fator de balanceamento é recalculado: FB(8) = h(esq) - h(dir) = 1 - 0 = +1.\n"
        "5. A altura total da subárvore enraizada em 8 permaneceu 2 (max(1, 0) + 1 = 2), a mesma que tinha antes da remoção. Consequentemente, a variação de altura NÃO se propaga para a raiz 10, mantendo FB(10) = 0."
    ))

    # Section 3: Código-Fonte Completo
    body_parts.append(make_heading("3. Código-Fonte Completo (Java)", 1))
    body_parts.append(make_paragraph(
        "O código foi desenvolvido em conformidade com as boas práticas de orientação a objetos, utilizando herança direta de ABP e tipagem estrita. Os blocos abaixo utilizam formatação monoespaçada (Consolas 9.5pt) com realce sintático no padrão da IDE Eclipse (palavras-chave em roxo/negrito, comentários em verde itálico, strings em azul)."
    ))

    for fname in files:
        body_parts.append(make_heading(f"Classe {fname}", 2))
        body_parts.append(format_code_block(codes[fname]))

    # Section 4: Saída de Testes
    body_parts.append(make_heading("4. Registro de Execução dos Testes no Terminal", 1))
    terminal_output = """======================================================================
          SISTEMA DE TESTES - ÁRVORE AVL (HERANÇA DE ABP)            
                   Aluna: Larissa Samara                              
======================================================================

[PASSO 1] Inserindo chaves iniciais: 10, 5, 15, 2, 8, 22...

Árvore resultante após inserções iniciais:
                                  10[0]
              5[0]                          15[-1]
    2[0]                8[0]                          22[0]

[PASSO 2] Inserindo chave 25...
Ao inserir 25 na subárvore direita de 22 (que é filho de 15):
 - 22 fica com FB = -1
 - 15 fica com FB = -2 (DESBALANCEAMENTO detectado!)

Como FB(15) = -2 e FB(22) = -1 (sinais iguais negativos):
>> Rotação Simples para a Esquerda (S.E.) aplicada no nó 15.
Aplicação das fórmulas de recálculo:
  FB'(15) = FB(15) + 1 - min(FB(22), 0) = -2 + 1 - (-1) = 0
  FB'(22) = FB(22) + 1 + max(FB'(15), 0) = -1 + 1 + 0 = 0

Árvore após rotação S.E. em 15:
                                  10[0]
              5[0]                                    22[0]
    2[0]                8[0]                15[0]               25[0]

[PASSO 3] Removendo chave 5...
Detalhe da implementação:
 1. O nó 5 possui dois filhos (2 e 8).
 2. Localiza o sucessor in-order: o menor nó da subárvore direita, que é o 8.
 3. Copia a chave 8 para a posição do 5.
 4. Remove o nó folha original 8 da subárvore direita.
 5. A subárvore direita de 8 diminuiu de altura. Recalcula FB(8):
    FB(8) = altura(esq=2) - altura(dir=vazio) = 1 - 0 = +1.
 6. Como a altura total da subárvore enraizada em 8 permaneceu 2,
    a alteração de altura NÃO se propaga para a raiz 10 (FB(10) continua 0).

Árvore resultante após remover 5:
                        10[0]
              8[1]                          22[0]
    2[0]                          15[0]               25[0]

======================================================================
>> Demonstração concluída com 100% de sucesso!"""
    body_parts.append(format_code_block(terminal_output))

    # Assemble Document XML
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
