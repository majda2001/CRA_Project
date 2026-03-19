import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'roleLabel',
  standalone: true
})
export class RoleLabelPipe implements PipeTransform {
  transform(value: string | undefined): string {
    if (!value) return '';
    switch (value) {
      case 'ADMIN': return 'Administrateur';
      case 'COLLABORATEUR': return 'Collaborateur';
      default: return value;
    }
  }
}
