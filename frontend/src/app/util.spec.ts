import { describe, expect, it } from 'vitest';
import { formatoNumero, formatoPolinomio, parsearCoeficientes } from './util';

describe('parsearCoeficientes', () => {
  it('lee coeficientes separados por comas, de menor a mayor grado', () => {
    expect(parsearCoeficientes('-4, 0, 1')).toEqual({ valores: [-4, 0, 1], error: null });
  });

  it('acepta espacios y punto y coma', () => {
    expect(parsearCoeficientes('1 2;3').valores).toEqual([1, 2, 3]);
  });

  it('acepta notación científica', () => {
    expect(parsearCoeficientes('1e-3,2').valores).toEqual([0.001, 2]);
  });

  it('rechaza un texto vacío', () => {
    expect(parsearCoeficientes('   ').error).not.toBeNull();
  });

  it('rechaza algo que no es un número', () => {
    const r = parsearCoeficientes('1, dos, 3');
    expect(r.valores).toEqual([]);
    expect(r.error).toContain('dos');
  });
});

describe('formatoPolinomio', () => {
  it('x^2 - 4', () => {
    expect(formatoPolinomio([-4, 0, 1])).toBe('x^2 - 4');
  });

  it('x^3 - 2x - 5', () => {
    expect(formatoPolinomio([-5, -2, 0, 1])).toBe('x^3 - 2x - 5');
  });

  it('coeficiente líder negativo y término de grado 1', () => {
    expect(formatoPolinomio([0, 3, -2])).toBe('-2x^2 + 3x');
  });

  it('constante y cero', () => {
    expect(formatoPolinomio([7])).toBe('7');
    expect(formatoPolinomio([0])).toBe('0');
  });
});

describe('formatoNumero', () => {
  it('quita ceros de relleno', () => {
    expect(formatoNumero(2.0000000000001)).toBe('2');
    expect(formatoNumero(1.5)).toBe('1.5');
  });

  it('deja pasar texto y nulos', () => {
    expect(formatoNumero('abc')).toBe('abc');
    expect(formatoNumero(null)).toBe('');
  });
});
