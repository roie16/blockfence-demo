const mkops = (from, to, template, nbytes = () => 0) => [...Array(to - from + 1).keys()].reduce((object, current) => ({
  ...object,
  [(from + current).toString(16)]: [`${template(current)}`, nbytes(current)]
}), {})

const mkinvalids = (from, to) => mkops(from, to, (i) => `INVALID(${(0x25 + from).toString(16)})`)

const opmap = {
  '00': ['STOP'],
  '01': ['ADD'],
  '02': ['MUL'],
  '03': ['SUB'],
  '04': ['DIV'],
  '05': ['SDIV'],
  '06': ['MOD'],
  '07': ['SMOD'],
  '08': ['ADDMOD'],
  '09': ['MULMOD'],
  '0a': ['EXP'],
  '0b': ['SIGNEXTEND'],
  ...mkinvalids(0x0c, 0x0f),
  '10': ['LT'],
  '11': ['GT'],
  '12': ['SLT'],
  '13': ['SGT'],
  '14': ['EQ'],
  '15': ['ISZERO'],
  '16': ['AND'],
  '17': ['OR'],
  '18': ['XOR'],
  '19': ['NOT'],
  '1b': ['SHL'],
  '1c': ['SHR'],
  ...mkinvalids(0x1d, 0x1f),
  '20': ['SHA3'],
  ...mkinvalids(0x21, 0x2f),
  '30': ['ADDRESS'],
  '31': ['BALANCE'],
  '32': ['ORIGIN'],
  '33': ['CALLER'],
  '34': ['CALLVALUE'],
  '35': ['CALLDATALOAD'],
  '36': ['CALLDATASIZE'],
  '37': ['CALLDATACOPY'],
  '38': ['CODESIZE'],
  '39': ['CODECOPY'],
  '3b': ['EXTCODESIZE'],
  '3c': ['EXTCODECOPY'],
  '3d': ['RETURNDATASIZE'],
  '3e': ['RETURNDATACOPY'],
  '40': ['BLOCKHASH'],
  '41': ['COINBASE'],
  '42': ['TIMESTAMP'],
  '44': ['DIFFICULTY'],
  '45': ['GASLIMIT'],
  '46': ['CHAINID'],
  '47': ['SELFBALANCE'],
  '48': ['BASEFEE'],
  ...mkinvalids(0x49, 0x4f),
  '50': ['POP'],
  '51': ['MLOAD'],
  '52': ['MSTORE'],
  '54': ['SLOAD'],
  '55': ['SSTORE'],
  '56': ['JUMP'],
  '57': ['JUMPI'],
  '59': ['MSIZE'],
  '5a': ['GAS'],
  '5b': ['JUMPDEST'],
  ...mkinvalids(0x5c, 0x5f),
  ...mkops(0x60, 0x7f, i => `PUSH${i + 1}`, i => i + 1),
  ...mkops(0x80, 0x8f, i => `DUP${i + 1}`),
  ...mkops(0x90, 0x9f, i => `SWAP${i + 1}`),
  ...mkops(0xa0, 0xa4, i => `LOG${i}`),
  ...mkinvalids(0xa5, 0xaf),
  'b0': ['PUSH'],
  'b1': ['DUP'],
  'b2': ['SWAP'],
  ...mkinvalids(0xb3, 0xef),
  'f0': ['CREATE'],
  'f1': ['CALL'],
  'f2': ['CALLCODE'],
  'f3': ['RETURN'],
  'f4': ['DELEGATECALL'],
  'f5': ['CREATE2'],
  ...mkinvalids(0xf6, 0xf9),
  'fa': ['STATICCALLREVERT'],
  ...mkinvalids(0xfb, 0xfc),
  'fd': ['REVERT'],
  ...mkinvalids(0xfe, 0xfe),
  'ff': ['SELFDESTRUCT'],
}

const parse = bytecode => {
  let i = 0;
  const ops = [];

  const parseOp = i => {
    const [ opcode, argsLength = 0 ] = opmap[bytecode[i]]
    return [ opcode, argsLength ? `0x${bytecode.slice(i + 1, i + argsLength + 1).join('')}` : null, i + 1 + argsLength ]
  }
  
  while(i < bytecode.length) {
    const op = parseOp(i);
    ops.push(op)
    i = op[2];
  }

  return ops;
}

parse(bytes.toString().substring(2).match(/.{2}/g));